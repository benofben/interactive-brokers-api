package MeanReversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

// need to handle rejected orders.  How do I find out an order has been rejected?

public class IBWrapper implements EWrapper{
    private EClientSocket m_client = new EClientSocket(this);
    Contract contract;
    
    // next unused orderId
    int id=1;
    
    // most recent bid and ask.  0 if not initialized.
	double bidPrice;
	double askPrice;
	
	// price (bid or ask), orderId
	ConcurrentHashMap<Double, Integer> activeTrades = new ConcurrentHashMap<Double, Integer>();
	
	// orderId, status
	// Custom Status: New, Matched
	// IB Status: PreSubmitted, Submitted, Filled, Cancelled, Inactive
	ConcurrentHashMap<Integer, String> orderStatus = new ConcurrentHashMap<Integer, String>();

	// orderId, side
	ConcurrentHashMap<Integer, String> orderSide = new ConcurrentHashMap<Integer, String>();

	// two lists of orderIds that have bill filled but not matched yet
	List<Integer> longTradeList = new ArrayList<Integer>();
	List<Integer> shortTradeList = new ArrayList<Integer>();
	
    public IBWrapper() {
		contract = new Contract();
		contract.m_symbol = "ES";
	    contract.m_secType = "FUT";
	    contract.m_expiry = "201412";
	    contract.m_exchange = "GLOBEX";
	    contract.m_currency = "USD";
	    
    	connect();
    	requestMarketData();    	
	}

	private void connect() {
		String host = "";
		int port = 7496;
		int clientId = 576;
		m_client.eConnect(host, port, clientId);
		if (m_client.isConnected()) {
			System.out.println("Connected to Tws server version " + m_client.serverVersion() + " at " + m_client.TwsConnectionTime());
		}
	}

	public void disconnect() {
		m_client.eDisconnect();
		System.out.println("Disconnected");
	}
	
	private void requestMarketData(){
		int tickerId=0;
	       
		String genericTickList = "";
		boolean snapshot = false;
        m_client.reqMktData(tickerId, contract, genericTickList, snapshot);
	}

	@Override
	public void error(Exception e) {
		System.out.println("error e: " + e.toString());
	}

	@Override
	public void error(String str) {
		System.out.println("error str: " + str);
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		System.out.println("error id: " + id + " errorMsg: " + errorMsg);
	}

	@Override
	public void connectionClosed() {
		System.out.println("connectionClose");
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		//System.out.println("tickPrice tickerId: " + tickerId + " field: " + field + " price: " + price);
		
		if(field==1)
		{
			//System.out.println("Bid changed to " + price);
			bidPrice=price;
		}
		else if(field==2)
		{
			//System.out.println("Ask changed to " + price);
			askPrice=price;
		}
		
		if(field==1 || field==2){
			place10orders();
		}
	}
	
	private synchronized void place10orders()
	{
		if (bidPrice==0 || askPrice==0)
			return;
		
		for(int i=0;i<5;i++)
		{
			placeOrder(bidPrice-i*0.25, "BUY");
			placeOrder(askPrice+i*0.25, "SELL");
		}
	}
	
	private synchronized void placeOrder(double price, String action){
		if(activeTrades.containsKey(price))
		{
			//System.out.println("Trade with price " + price + " already exists.");
			return;
		}
		
		System.out.println("Going to " + action + " at " + price + ".");

		Order order = new Order();
		order.m_lmtPrice=price;
		order.m_orderType="LMT";
		order.m_totalQuantity=1;
		order.m_action=action;
		m_client.placeOrder(id, contract, order);

		activeTrades.put(price, id);
		orderStatus.put(id, "New");
		orderSide.put(id, action);

		id++;
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		//System.out.println("tickSize tickerId: " + tickerId + " field: " + field + " size: " + size);
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
		System.out.println("tickOptionComputation");
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		//System.out.println("tickGeneric tickerId: " + tickerId + " tickType: " + tickType + " value: " + value);
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		//System.out.println("tickString tickerId: " + tickerId + " tickType: " + tickType + " value: " + value);
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		System.out.println("tickEFP");
	}

	@Override
	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		System.out.println("orderStatus status: " + status + " orderId: " + orderId);
		orderStatus.put(orderId, status);
		
		if(status.equals("Filled")){
			// Now we want to match this with another trade before we trade again at this price
			String side = orderSide.get(orderId);
			
			if(side.equals("BUY")){
				longTradeList.add(orderId);
			}
			else if(side.equals("SELL")){
				shortTradeList.add(orderId);
			}
			
			matchFilledTrades();
		}
		deleteCompletedTrades();
	}
	
	private synchronized void matchFilledTrades(){
		if(!longTradeList.isEmpty() && !shortTradeList.isEmpty()){
			int shortOrderId = shortTradeList.remove(0);
			orderStatus.put(shortOrderId, "Matched");

			int longOrderId = longTradeList.remove(0);
			orderStatus.put(longOrderId, "Matched");
			
			System.out.println("Matched two trades.");
		}		
	}
	
	private synchronized void deleteCompletedTrades(){
		//System.out.println("Checking for trades to delete.");
		for(Entry<Double, Integer> entry : activeTrades.entrySet()) {
		    double price = entry.getKey();
		    int orderId = entry.getValue();
	
		    String status=orderStatus.get(orderId);
		    
		    if(status.equals("Matched") || status.equals("Cancelled")){
		    	activeTrades.remove(price);
//		    	orderStatus.remove(orderId);
//		    	orderSide.remove(orderId);
		    	System.out.println("Removed a trade with price: " + price);
		    }
		}
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		//System.out.println("openOrder");
	}

	@Override
	public void openOrderEnd() {
		System.out.println("openOrderEnd");
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		System.out.println("updateAccountValue");
	}

	@Override
	public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		System.out.println("updatePortfolio");
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		System.out.println("updateAccountTime");
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		System.out.println("accountDownloadEnd");
	}

	@Override
	public void nextValidId(int orderId) {
		//System.out.println("nextValidId orderId: " + orderId);
		id=orderId;
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println("contractDetails");
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println("bondContactDetails");
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		System.out.println("contractDetailsEnd");
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		//System.out.println("execDetails");
	}

	@Override
	public void execDetailsEnd(int reqId) {
		System.out.println("execDetailsEnd");
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		System.out.println("updateMktDepth");
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
		System.out.println("updateMktDepthL2");
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		System.out.println("updateNewsBulletin");
	}

	@Override
	public void managedAccounts(String accountsList) {
		//System.out.println("managedAccounts");
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		System.out.println("recieveFA");
	}

	@Override
	public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
		System.out.println("historicalData");
	}

	@Override
	public void scannerParameters(String xml) {
		System.out.println("scannerParameters");
	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
		System.out.println("scannerData");
	}

	@Override
	public void scannerDataEnd(int reqId) {
		System.out.println("scannerDataEnd");
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
		System.out.println("realtimeBar");
	}

	@Override
	public void currentTime(long time) {
		System.out.println("currentTime");
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		System.out.println("fundamentalData");
	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		System.out.println("deltaNeutralValidation");
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		System.out.println("tickSnapshotEnd");
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		System.out.println("marketDataType");
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		//System.out.println("commissionReport");
	}

	@Override
	public void position(String account, Contract contract, int pos, double avgCost) {
		System.out.println("position");
	}

	@Override
	public void positionEnd() {
		System.out.println("positionEnd");
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		System.out.println("accountSummary");
	}

	@Override
	public void accountSummaryEnd(int reqId) {
		System.out.println("accountSummaryEnd");
	}
}