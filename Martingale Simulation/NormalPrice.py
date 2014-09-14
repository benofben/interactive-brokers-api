import random

def run():
	results=[]
	for unused_i in range(0,1000):
		result=simulateOneMartingale()
		results.append(result)
		
	import numpy
	print(numpy.average(results))

	plot(results)

def simulateOneMartingale():
	initialMargin=5060
	maintenanceMargin=4600
	
	tickPosition=0
	capital=20000
	numberOfOpenContracts=1
	
	while numberOfOpenContracts!=0:
		if random.random()<0.5-tickPosition/100: 
			# price went up a tick
			tickPosition+=1
			capital+=numberOfOpenContracts*25
	
			# We're long and we sell
			numberOfOpenContracts-=1
		else:
			#price went down a tick
			tickPosition-=1
			capital-=numberOfOpenContracts*25
			
			if capital/numberOfOpenContracts<maintenanceMargin:
				# We're long and forced to sell due to margin
				numberOfOpenContracts-=1
			elif initialMargin<capital-numberOfOpenContracts*maintenanceMargin:
				# We're long and we buy
				numberOfOpenContracts+=1
			
		#print(str(numberOfOpenContracts) + ' ' + str(capital))
			
	result=capital-20000
	return result

def plot(results):
	import matplotlib.pyplot as plt
	plt.hist(results)
	plt.show()
	import numpy
	print(numpy.average(results))
	
run()