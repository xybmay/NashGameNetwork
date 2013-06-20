package NashGameNetwork;

// the model, in charge of the creation of the agent, the world and the execution of the programme. 
import java.awt.geom.Arc2D.Float;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.VNQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.context.space.graph.*;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.space.graph.Network;
import repast.simphony.context.space.graph.*;

  @SuppressWarnings("unchecked")
  public class GameBuilder extends DefaultContext implements ContextBuilder<Agent>{
  //Here we will first construct the world which the agent lives in it.
	//first creates the context, then add the agent 
	//then creates a small world complex network
	//after the agents' complex interaction network is done,
	//the model will random schedule the agent's step method
	private ArrayList agentlist= new ArrayList();
	private int actionNumber;
	private final  int stop=5000;
	private double  totalPayoff;
	private double  totalRealPayoff;
	private int numberCooperation;
	private int numberDefection;
	
	public Context build(Context<Agent> context){
		Parameters p = RunEnvironment.getInstance().getParameters();
		int num = (Integer)p.getValue("number of agent");
		int length = (Integer)p.getValue("neighbor size");
		double wsProbability=(Double)p.getValue("WS probability");
		actionNumber = (Integer)p.getValue("number of agent in one step");
		numberCooperation= (Integer)p.getValue("number of cooperation");
		numberDefection=num-numberCooperation;
		int agentID=0;
        agentlist.clear();
		//add the agent to the context
		for(int i=0;i<num;i++ ){
			//System.out.println("creat new agent");
			Agent agent=new Agent(); 
			agent.setID(i);
			context.add(agent);
			agentlist.add((Object)agent);
			agent.setCurrentStrategy('D');
		}
		
		NetworkGenerator gen= new WattsBetaSmallWorldGenerator(wsProbability,length,false);
		NetworkBuilder builder=new NetworkBuilder("Living world",context,false);
		builder.setGenerator(gen);
		Network net = builder.buildNetwork();
		System.out.println("the network creation is done!!!");
        System.out.println(" the network degree    "+net.getDegree());
        
		//set the initial status
		int numbertemp=0;
		while( numbertemp<numberCooperation){
			RandomHelper.createUniform();
			int random=RandomHelper.nextIntFromTo(0, num-1);
			if (((Agent)agentlist.get(random)).getCurrentStrategy()=='D')
			           	{
				           ((Agent)agentlist.get(random)).setCurrentStrategy('C');
				           numbertemp++;
			           	}
			else continue;
		}
		
//		for(int j=0;j<agentlist.size();j++){
//			System.out.println("agent   "+((GameAgent)agentlist.get(j)).getID()+"    "+((GameAgent)agentlist.get(j)).getCurrentStrategy());
//		}
		
		int numSocialPreference = (Integer)p.getValue("number of social preference agent");
		int sum=0;
		
		while(sum<numSocialPreference){
		    RandomHelper.createUniform();
			int random=RandomHelper.nextIntFromTo(0, num-1);
		    if (!((Agent)agentlist.get(random)).isIfSocialPreference()){
		    ((Agent)agentlist.get(random)).setIfSocialPreference(true);
		    sum++;
		    }
		}
		  
		System.out.println("set the agent which is the social preference type  is done!!!");
		// for(int k=0;k<agentlist.size();k++){
	   	    	//System.out.println("PayoffCC is set to "+((GameAgent)agentlist.get(k)).getPayoffOfCC()+"   ID is   "
	   	    			//+((GameAgent)agentlist.get(k)).getID()) ;
	   	    	//System.out.println("step   ");
	   	  //  }
		return context;
	  }
	
	@ScheduledMethod(start=0,interval=1)
	  public void step(){
 	    Parameters p = RunEnvironment.getInstance().getParameters();
      actionNumber = (Integer)p.getValue("number of agent in one step");
      Agent[] runlist= new Agent[actionNumber];
      int num = (Integer)p.getValue("number of agent");
      Object[] alist=new Object[num];
      alist=this.toArray();
      for(int i=0;i<alist.length;i++){
      	agentlist.add(alist[i]);
      }
             
//      int j=0;
// 	    while(j<actionNumber){
// 	    	int RunID=RandomHelper.nextIntFromTo(0, num-1);
// 	    	ArrayList RunIDList = new ArrayList();
// 	    	GameAgent temp=(GameAgent)agentlist.get(RunID);
//	        if (!RunIDList.contains(RunID)){
//	        	RunIDList.add(RunID);
//	           	runlist[j]=temp;
//	            j+=1;
//	        }
//	    }
 	    
 	    for(int k=0;k<alist.length;k++){
 	    	((Agent)alist[k]).step1();
 	    	//System.out.println("step 1");
 	    	//System.out.println("step   ");
 	    }
 	    
// 	 for(int k=0;k<agentlist.size();k++){
//	    	((GameAgent)agentlist.get(k)).step1();
//	    	//System.out.println("step   ");
//	    }
 	    
  	 for(int k=0;k<alist.length;k++){
  		 ((Agent)alist[k]).step2();
  		 //System.out.println("step 2");
	    	//System.out.println("step   ");
	    }
 	    
  	 for(int k=0;k<alist.length;k++){
  		 ((Agent)alist[k]).step3();
	    	//System.out.println("step   ");
	    }
  	 
  	 for(int k=0;k<alist.length;k++){
  		 ((Agent)alist[k]).step4();
  		 //System.out.println("step 3");
	    	//System.out.println("step   ");
	    }
  	 
// 	for(int k=0;k<agentlist.size();k++){
//  	((GameAgent)agentlist.get(k)).step2();
//  	//System.out.println("step   ");
//  }
 	 
 	   for(int k=0;k<alist.length;k++){
 		((Agent)alist[k]).postStep();
 		//System.out.println("step 4");
  	//System.out.println("Poststep   ");
    }
 	   
// 	for(int k=0;k<agentlist.size();k++){
//  	((GameAgent)agentlist.get(k)).postStep();
//  	//System.out.println("step   ");
//   }
 }
	
	@ScheduledMethod(start=stop)
	public void end(){
		try {
			FileWriter  fwresult = new FileWriter("./SimulationDataOne.txt",true);
			BufferedWriter bwresult = new BufferedWriter(fwresult);
          PrintWriter pwresult= new PrintWriter(bwresult);
          numberCooperation=0;
          numberDefection=0;
          
          Parameters p = RunEnvironment.getInstance().getParameters();
           int num = (Integer)p.getValue("number of agent");
//          Object[] alist=new Object[num];
//          alist=this.toArray();
//          for(int i=0;i<alist.length;i++){
//          	agentlist.add(alist[i]);
//          }
          
          for(int i=0;i<agentlist.size();i++){
              if(((Agent) (agentlist.get(i))).getChoosedStrategy()=='C')numberCooperation++;
              else numberDefection++;
              totalPayoff+=((Agent) (agentlist.get(i))).getCurrentPayoff();
              totalRealPayoff+=((Agent) (agentlist.get(i))).getCurrentSocialPayoff();
          }
          
          double payoffOfDC=(Double)p.getValue("payoffOfDC");
          int numberOfSocialAgent=(Integer) p.getValue("number of social preference agent");
          double alpha=(Double)p.getValue("alpha of social preference function");
          double beta=(Double)p.getValue("beta of social preference function");
          double theta=(Double)p.getValue("theta of social preference coefficient");
          double wsProbability=(Double)p.getValue("WS probability");
          int degree=(Integer)p.getValue("neighbor size");
          pwresult.print("       ");
          pwresult.printf("%.15f",((double)(numberCooperation)/(numberCooperation+numberDefection)));
          pwresult.print("       ");
          pwresult.printf("%.15f",((double)(numberDefection)/(numberCooperation+numberDefection)));
          pwresult.print("       ");
          pwresult.printf("%.1f",(RunEnvironment.getInstance()).getCurrentSchedule().getTickCount()); 
          pwresult.print("       ");
          pwresult.printf("%.1f",totalPayoff); 
          pwresult.print("       ");
          pwresult.printf("%f",payoffOfDC); 
          pwresult.print("       ");
          pwresult.printf("%d",numberOfSocialAgent); 
          pwresult.print("       ");
          pwresult.printf("%d",degree); 
          pwresult.print("       ");
          pwresult.printf("%f",wsProbability); 
          pwresult.print("       ");
          pwresult.printf("%f",alpha); 
          pwresult.print("       ");
          pwresult.printf("%f",beta); 
          pwresult.print("       ");
          pwresult.printf("%f",theta); 
          pwresult.println("       ");
          pwresult.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	   System.out.println("finished write to the file");
		(RunEnvironment.getInstance()).endAt(stop);
	}
}
