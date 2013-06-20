package NashGameNetwork;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

public class Agent {
  private int ID;
	private char currentStrategy;
	private char previousStrategy;
	private char initialStrategy;
	private double currentPayoff;
	private double previouisPayoff;
	private double currentSocialPayoff;
	private double previouisSocialPayoff;
	private double currentSocialUtility;
	private double previousSocialUtility;
	private double alpha;
	private double beta;
	private double lambda;
	private double theta;
	private  Network network;  //the network agent located in 
	private boolean ifSocialPreference;
	private char choosedStrategy;
	
	public Agent( ){
		//initialize  agent  parameter
		Parameters p = RunEnvironment.getInstance().getParameters();
        this.network=null;
		this.lambda=(Double)p.getValue("lambda");
		this.alpha = (Double)p.getValue("alpha of social preference function");
		this.beta = (Double)p.getValue("beta of social preference function");
		this.theta= (Double)p.getValue("theta of social preference coefficient");
		this.currentStrategy='N';
		Model context = (Model) ContextUtils.getContext(this);
        Network network=(Network)context.getProjection("Living world");
        ifSocialPreference=true;
	}
	
	public static double play(Agent agent1,Agent agent2){
		double result=0;
		switch (agent1.getCurrentStrategy()){
			case 'H':
			if (agent2.getCurrentStrategy()=='H')
				return result=0;
			else if (agent2.getCurrentStrategy()=='M')
				return result=0;
			else if (agent2.getCurrentStrategy()=='L')
				return result=0;
      	   case'M':
				if (agent2.getCurrentStrategy()=='H')
					return result=0;
				else if (agent2.getCurrentStrategy()=='M')
					return result=0.5;
				else if (agent2.getCurrentStrategy()=='L')
					return result=0.7;
      	   case 'L':
				if (agent2.getCurrentStrategy()=='H')
					return result=0;
				else if (agent2.getCurrentStrategy()=='M')
					return result=0.3;
				else if (agent2.getCurrentStrategy()=='L')
					return result=0.3;
	        }
		return 0;
	}

	public double computePayoff() {
	    //compute the payoff and the socialPreference if needed in initial status
        currentPayoff=0;
        
	    Iterable neighbors=network.getAdjacent(this);
	    //System.out.println("begin compute the payoff  ");
	    
	    for (Object o : neighbors) {
	    	currentPayoff+=(double)play(this,(Agent)o);
	       			//System.out.println("pay off is computing   added   "+((GameAgent)o).getID()+"  "+singlePlayCooperation(this,(GameAgent)o));
	    }  
	    //System.out.println(" payoff computed  for  cooperation  of agent  "+this.ID+"    "+currentPayoffCooperation);
	     return currentPayoff;
	}
	

    public void computeSocialPreference(){
    	
    	currentSocialPayoff=0;
	    Iterable neighbors=network.getAdjacent(this);  
        Parameters p = RunEnvironment.getInstance().getParameters();
		double alpha = (Double)p.getValue("alpha of social preference function");
		//double beta = (Double)p.getValue("beta of social preference function");
		double beta=alpha;
		double theta= (Double)p.getValue("theta of social preference coefficient");
		int length=0;
		double sumAlpha=0;
		double sumBeta=0;
		double totalWelfare=0; 
		
       	double temp1=0;
       	double temp2=0;
       	for (Object o : neighbors) {
       		if(currentPayoff>=((Agent)o).getCurrentPayoff()){
       				temp2=currentPayoff-((Agent)o).getCurrentPayoff();
       				sumBeta+=temp2;
       		}
       		else {
       				temp1=((Agent)o).getCurrentPayoff()-currentPayoff;
       				sumAlpha+=temp1;
       		 }
       		totalWelfare+=((Agent)o).getCurrentPayoff();
       		length++;
       		//System.out.println("length     "+length);
       	}
	   
       if(!ifSocialPreference)
    	   {
    	   currentSocialPayoff=currentPayoff;
    	   }
       else currentSocialPayoff=currentPayoff-alpha*sumAlpha/length-beta*sumBeta/length+theta*totalWelfare;
	}
    
    public void randomMatchAndChooseStrategy(){
    	//this method is used as the updating rule when agent random choose a neighbor
    	//and change his strategy according the utility difference
    	double prob=0;
    	Agent  random;
    	RandomHelper.init();
    	double randomProb=RandomHelper.nextDoubleFromTo(0, 1);
        random=(Agent)network.getRandomAdjacent(this);
        Iterable neighbors=network.getAdjacent(this);  
        
//        double max=this.totalPayoff;
//        for (Object o : neighbors) {
//       		if(((GameAgent)o).getTotalPayoff()>max){
//       			//System.out.println("choose a strategy");
//       			max=((GameAgent)o).getTotalPayoff();
//       		}
//       		else continue;
//       	}
        
        int lengthD=0;
		double sumAlphaD=0;
		double sumBetaD=0;
		double totalWelfareD=0; 
		
        //System.out.println("random   ID   "+random.getID()+"   "+this.getID());
    	double temp1D=0;
       	double temp2D=0;
       	double ramdomNeighborayoff=0;
       	
		if(isIfSocialPreference()){
       		for (Object o : neighbors) {
           		if(random.getCurrentSocialPayoff()>=((Agent)o).getCurrentSocialPayoff()){
           				temp2D=random.getCurrentSocialPayoff()-((Agent)o).getCurrentSocialPayoff();
           				sumBetaD+=temp2D;
           		}
           		else {
           				temp1D=((Agent)o).getCurrentSocialPayoff()-random.getCurrentSocialPayoff();
           				sumAlphaD+=temp1D;
           		 }
           		totalWelfareD+=((Agent)o).getCurrentPayoff();
           		
       		
       		ramdomNeighborayoff=random.getCurrentPayoff()-alpha*sumAlphaD/lengthD-beta*sumBetaD/lengthD;
            //+theta*(totalWelfareD+difference);
       		 
       	  }
		}
       	else{
       		ramdomNeighborayoff=random.getCurrentPayoff();
       	 }
       	
        prob=(double)1/(1+Math.exp(lambda*(this.getCurrentSocialPayoff()-ramdomNeighborayoff)));
	    //prob=(random.getCurrentPayoff()-this.getCurrentPayoff())/max;
	    //System.out.println("Probability    "+prob+ "   random is    "+randomProb);
	    if (randomProb<=prob) {
	    	//System.out.println("    random strategy  is  "+random.getCurrentStrategy());
	    	this.choosedStrategy=random.getCurrentStrategy();
	    }
	    else{
	    	this.choosedStrategy=this.getCurrentStrategy();
	    	//System.out.println("strategy no changed");
	    };
	      
    }
		
	/**
	 * @return the iD
	 */
	public int getID() {
		return ID;
	}
	/**
	 * @param iD the iD to set
	 */
	public void setID(int iD) {
		ID = iD;
	}
	/**
	 * @return the currentStrategy
	 */
	public char getCurrentStrategy() {
		return currentStrategy;
	}
	/**
	 * @param currentStrategy the currentStrategy to set
	 */
	public void setCurrentStrategy(char currentStrategy) {
		this.currentStrategy = currentStrategy;
	}
	/**
	 * @return the previousStrategy
	 */
	public char getPreviousStrategy() {
		return previousStrategy;
	}
	/**
	 * @param previousStrategy the previousStrategy to set
	 */
	public void setPreviousStrategy(char previousStrategy) {
		this.previousStrategy = previousStrategy;
	}
	/**
	 * @return the currentPayoff
	 */
	public double getCurrentPayoff() {
		return currentPayoff;
	}
	/**
	 * @param currentPayoff the currentPayoff to set
	 */
	public void setCurrentPayoff(double currentPayoff) {
		this.currentPayoff = currentPayoff;
	}
	/**
	 * @return the previouisPayoff
	 */
	public double getPreviouisPayoff() {
		return previouisPayoff;
	}
	/**
	 * @param previouisPayoff the previouisPayoff to set
	 */
	public void setPreviouisPayoff(double previouisPayoff) {
		this.previouisPayoff = previouisPayoff;
	}
	/**
	 * @return the currentSocialUtility
	 */
	public double getCurrentSocialUtility() {
		return currentSocialUtility;
	}
	/**
	 * @param currentSocialUtility the currentSocialUtility to set
	 */
	public void setCurrentSocialUtility(double currentSocialUtility) {
		this.currentSocialUtility = currentSocialUtility;
	}
	/**
	 * @return the previousSocialUtility
	 */
	public double getPreviousSocialUtility() {
		return previousSocialUtility;
	}
	/**
	 * @param previousSocialUtility the previousSocialUtility to set
	 */
	public void setPreviousSocialUtility(double previousSocialUtility) {
		this.previousSocialUtility = previousSocialUtility;
	}
	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}
	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	/**
	 * @return the beta
	 */
	public double getBeta() {
		return beta;
	}
	/**
	 * @param beta the beta to set
	 */
	public void setBeta(double beta) {
		this.beta = beta;
	}
	/**
	 * @return the lambda
	 */
	public double getLambda() {
		return lambda;
	}
	/**
	 * @param lambda the lambda to set
	 */
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}
	/**
	 * @return the network
	 */
	public Network getNetwork() {
		return network;
	}
	/**
	 * @param network the network to set
	 */
	public void setNetwork(Network network) {
		this.network = network;
	}

	public char getInitialStrategy() {
		return initialStrategy;
	}

	public void setInitialStrategy(char initialStrategy) {
		this.initialStrategy = initialStrategy;
	}

	public double getCurrentSocialPayoff() {
		return currentSocialPayoff;
	}

	public void setCurrentSocialPayoff(double currentSocialPayoff) {
		this.currentSocialPayoff = currentSocialPayoff;
	}

	public double getPreviouisSocialPayoff() {
		return previouisSocialPayoff;
	}

	public void setPreviouisSocialPayoff(double previouisSocialPayoff) {
		this.previouisSocialPayoff = previouisSocialPayoff;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public boolean isIfSocialPreference() {
		return ifSocialPreference;
	}

	public void setIfSocialPreference(boolean ifSocialPreference) {
		this.ifSocialPreference = ifSocialPreference;
	}

	public char getChoosedStrategy() {
		return choosedStrategy;
	}

	public void setChoosedStrategy(char choosedStrategy) {
		this.choosedStrategy = choosedStrategy;
	}

	public void step1() {
		// TODO Auto-generated method stub
		
	}

	public void step2() {
		// TODO Auto-generated method stub
		
	}

	public void step3() {
		// TODO Auto-generated method stub
		
	}

	public void step4() {
		// TODO Auto-generated method stub
		
	}

	public void postStep() {
		// TODO Auto-generated method stub
		
	}
}
