package MeanReversion;

import java.util.Scanner;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

public class IBWrapper implements EWrapper{
    private EClientSocket m_client = new EClientSocket( this);

    public IBWrapper() {
    	connect();
    	requestMarketData();

    	Scanner keyboard = new Scanner(System.in);
    	keyboard.nextLine();	
    	keyboard.close();
    	
    	disconnect();
	}

	private void connect() {
		String host = "";
		int port = 7496;
		int clientId = 123;
		m_client.eConnect(host, port, clientId);
		if (m_client.isConnected()) {
			System.out.println("Connected to Tws server version " + m_client.serverVersion() + " at " + m_client.TwsConnectionTime());
		}
	}

	private void disconnect() {
		m_client.eDisconnect();
		System.out.println("Disconnected");
	}
	
	private void requestMarketData(){
		int tickerId=0;
		Contract contract = new Contract();
		contract.m_symbol = "ES";
	    contract.m_secType = "FUT";
	    contract.m_expiry = "201412";
	    contract.m_exchange = "GLOBEX";
	    contract.m_currency = "USD";
	       
		String genericTickList = "";
		boolean snapshot = false;
        m_client.reqMktData(tickerId, contract, genericTickList, snapshot);
	}

	@Override
	public void error(Exception e) {
		System.out.println("error 1: " + e.toString());
	}

	@Override
	public void error(String str) {
		System.out.println("error 2: " + str);
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		System.out.println("error 3: " + errorMsg);
	}

	@Override
	public void connectionClosed() {
		System.out.println("connectionClose");
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		System.out.println("tickPrice: " + tickerId + " " + field + " " + price);
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		System.out.println("tickSize: " + tickerId + " " + field + " " + size);
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
		System.out.println("tickOptionComputation");
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		System.out.println("tickGeneric: " + tickerId + " " + tickType + " " + value);
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		System.out.println("tickString: " + " " + tickerId + " " + tickType + " " + value);
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		System.out.println("tickEFP");
	}

	@Override
	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		System.out.println("orderStatus");
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		System.out.println("openOrder");
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
		System.out.println("nextValidId");
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
		System.out.println("execDetails");
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
		System.out.println("managedAccounts");
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
		System.out.println("commissionReport");
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