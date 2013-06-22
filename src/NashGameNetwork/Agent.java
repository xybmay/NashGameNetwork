package NashGameNetwork;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
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
	private double alpha;
	private double beta;
	private double lambda;
	private double theta;
	private boolean ifSocialPreference;
	private char choosedStrategy;
	private Context context;
	private  Network network;  //the network agent located in 
	
	public Agent(Context<Agent> context ){
		//initialize  agent  parameter
		Parameters p = RunEnvironment.getInstance().getParameters();
        this.network=null;
		this.lambda=(Double)p.getValue("lambda");
		this.alpha = (Double)p.getValue("alpha of social preference function");
		this.beta = (Double)p.getValue("beta of social preference function");
		//this.theta= (Double)p.getValue("theta of social preference coefficient");
		this.currentStrategy='L';
		this.context=context;
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
				return result=0.7;
      	   case'M':
				if (agent2.getCurrentStrategy()=='H')
					return result=0;
				else if (agent2.getCurrentStrategy()=='M')
					return result=0.5;
				else if (agent2.getCurrentStrategy()=='L')
					return result=0.5;
      	   case 'L':
				if (agent2.getCurrentStrategy()=='H')
					return result=0.3;
				else if (agent2.getCurrentStrategy()=='M')
					return result=0.3;
				else if (agent2.getCurrentStrategy()=='L')
					return result=0.3;
	        }
		return 0;
	}

	public double computeOneRoundPayoff() {
	    //compute the payoff and the socialPreference if needed in initial status
        currentPayoff=0;
        //System.out.println("context   "+context.size());
        network=(Network)context.getProjection("world");
        ifSocialPreference=true;
        
	    Iterable neighbors=network.getAdjacent(this);
	    //System.out.println("begin compute the payoff  ");
	    
	    for (Object o : neighbors) {
	    	currentPayoff+=(double)play(this,(Agent)o);
	    }  
	     return currentPayoff;
	}
	

    public void computeCurrentRoundSocialPreference(){
    	
    	currentSocialPayoff=0;
	    Iterable neighbors=network.getAdjacent(this);  
        Parameters p = RunEnvironment.getInstance().getParameters();
		double alpha = (Double)p.getValue("alpha of social preference function");
		double beta=alpha;
		int length=0;
		double sumAlpha=0;
		double sumBeta=0;
		double totalWelfare=0; 
		
       	double temp1=0;
       	double temp2=0;
       	length=network.getDegree(this);
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
       		//System.out.println("length     "+length);
       	}
	   
       if(!ifSocialPreference)
    	   {
    	   currentSocialPayoff=currentPayoff;
    	   }
       else currentSocialPayoff=currentPayoff-alpha*sumAlpha/length-beta*sumBeta/length;
    		   // +theta*totalWelfare;
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
       	double assumedRamdomNeighborSocialpayoff=0;
       	
       	lengthD=network.getDegree(this);
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
           		//totalWelfareD+=((Agent)o).getCurrentPayoff();
       		
       	 assumedRamdomNeighborSocialpayoff=random.getCurrentPayoff()-alpha*sumAlphaD/lengthD-beta*sumBetaD/lengthD;
            //+theta*(totalWelfareD+difference);
       		 
       	  }
		}
       	else{
       		assumedRamdomNeighborSocialpayoff=random.getCurrentPayoff();
       	 }
       	
        prob=(double)1/(1+Math.exp(lambda*(this.getCurrentSocialPayoff()-assumedRamdomNeighborSocialpayoff)));
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
	
 //   @ScheduledMethod(start = 1, interval = 1, priority = -1)
	public void step1() {
    	computeOneRoundPayoff();
    	System.out.println("ID:    "+ID+"    payoff    "+this.getCurrentPayoff());
    	//play the game with the neighbors and get the payoff.  
	}

	public void step2() {
		// according the payoff,  computing the payoff when the agent displays social preference;
		computeCurrentRoundSocialPreference();
	}

	public void step3() {
		// according the payoff and the randomly chosen neighbor,  choose the strategy next
		randomMatchAndChooseStrategy( );
	}

	public void postStep() {
		// update the status after the the choice
		currentStrategy=choosedStrategy; //used for the next step
		previouisPayoff=currentPayoff;
		previouisSocialPayoff=currentSocialPayoff;
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
}
