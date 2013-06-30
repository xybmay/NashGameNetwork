package NashGameNetwork;

// the model, in charge of the creation of the agent, the world and the execution of the program. 
import java.awt.geom.Arc2D.Float;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.runner.Runner;

import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.Schedule;
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
  public class Model extends DefaultContext implements ContextBuilder<Agent>{
    //Here we will first construct the world which the agent lives in it.
	//first creates the context, then add the agent 
	//then creates a small world complex network
	//after the agents' complex interaction network is done,
	//the model will random schedule the agent's step method
	private ArrayList agentlist= new ArrayList();
	private int actionNumber;
	private final  int stop=2000;
	private double  totalPayoff;
	private double  totalSocialPreferencePayoff;
	private int numberOfLStrategy;
	private int numberOfMStrategy;
	private int numberOfHStrategy;
	private boolean recorded3000;
	private boolean recorded4000;
	private boolean recorded4500;
	private boolean recorded4900;
	 
	public Context<Agent> build(Context<Agent>context){
		context.setId("NashGameNetwork");
		Parameters p = RunEnvironment.getInstance().getParameters();
		int numberOfAgent = (Integer)p.getValue("number of agent");
		int length = (Integer)p.getValue("neighbor size");
		double wsProbability=(Double)p.getValue("WS probability");
		actionNumber = (Integer)p.getValue("number of agent in one step");
		numberOfLStrategy= (Integer)p.getValue("number of L strategy");
		numberOfMStrategy= (Integer)p.getValue("number of M strategy");
		numberOfHStrategy= (Integer)p.getValue("number of H strategy");
        agentlist.clear();
		//add the agent to the context
        NetworkGenerator gen= new WattsBetaSmallWorldGenerator(wsProbability,length,false);
		NetworkBuilder builder=new NetworkBuilder("world",context,false);
		recorded3000=false;
		recorded4000=false;
		recorded4500=false;
		recorded4900=false;

        for(int i=0;i<numberOfAgent;i++ ){
			Agent agent=new Agent(context); 
			agent.setID(i);
			context.add(agent);
			agentlist.add((Agent)agent);
			agent.setCurrentStrategy('L');
			//System.out.println("new agent created:       "+context.size());
		}
		
		builder.setGenerator(gen);
		Network net = builder.buildNetwork();
		System.out.println("the network creation is done!!!");
       // System.out.println(" the network degree    "+net.getDegree());
        
		//set the initial status
		int numbertemp=0;
		while( numbertemp<numberOfMStrategy){
			RandomHelper.createUniform();
			int random=RandomHelper.nextIntFromTo(0, numberOfAgent-1);
			if (((Agent)agentlist.get(random)).getCurrentStrategy()=='L')
			           	{
				           ((Agent)agentlist.get(random)).setCurrentStrategy('M');
				           numbertemp++;
			           	}
			else continue;
		}
		
		numbertemp=0;
		while( numbertemp<numberOfHStrategy){
			RandomHelper.createUniform();
			int random=RandomHelper.nextIntFromTo(0, numberOfAgent-1);
			if ((((Agent)agentlist.get(random)).getCurrentStrategy()=='L') ||
				 (((Agent)agentlist.get(random)).getCurrentStrategy()=='M'))           	
			          {
				           ((Agent)agentlist.get(random)).setCurrentStrategy('H');
				           numbertemp++;
			           	}
			else continue;
		}
		
		int numSocialPreference = (Integer)p.getValue("number of social preference agent");
		int sum=0;
		
		//System.out.println(" number of social  preference   "+numSocialPreference);
		while(sum<numSocialPreference){
		    RandomHelper.createUniform();
			int random=RandomHelper.nextIntFromTo(0,numSocialPreference -1);
		    if (!((Agent)agentlist.get(random)).isIfSocialPreference()){
		    ((Agent)agentlist.get(random)).setIfSocialPreference(true);
		    sum++;
		   // System.out.println(" set social preference   ");
		    }
		}
		  
		//System.out.println("set the agent which is the social preference type  is done!!!");
		System.out.println("Simulation will begin");
		
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters paramsEnd = ScheduleParameters.createOneTime
				(stop, -1);
		ScheduleParameters paramsStatis = ScheduleParameters.createRepeating(1, 1);
		ScheduleParameters paramsStop = ScheduleParameters.createRepeating(100, 1);
		ScheduleParameters paramsInEvolution = ScheduleParameters.createRepeating(1, 1);
				
		//schedule.schedule(paramsStatis , this , "statis" );
		//schedule.schedule(paramsEnd , this , "end" );
		schedule.schedule(paramsInEvolution , this , "resultInEvolution" );
		schedule.schedule(paramsStop , this , "stopIfStable" );
		schedule.schedule(paramsEnd , this , "recordToFile" );
		return context;
	  }

	public void statis(){
		 numberOfLStrategy=0;
         numberOfMStrategy=0;
         numberOfHStrategy=0;
         
         Parameters p = RunEnvironment.getInstance().getParameters();
          int num = (Integer)p.getValue("number of agent");
         
         for(int i=0;i<agentlist.size();i++){
             if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='L') numberOfLStrategy++;
             else if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='M') numberOfMStrategy++;
             else numberOfHStrategy++;
	   }
        // System.out.println("strategy L  M   H "+numberOfLStrategy+"   "+numberOfMStrategy+"  "+numberOfHStrategy);
	}
	
	public void stopIfStable(){
		numberOfMStrategy=0; 
		
		for(int i=0;i<agentlist.size();i++){
          if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='M') numberOfMStrategy++;
		 }
		
		 //System.out.println("strategy  M  "+numberOfMStrategy);
		 
         if (numberOfMStrategy==5000){
			System.out.println(" The current Run is ended and now we write the results to a file ");
	    	recordToFile();
         }
	}
		
	// record the milestone in the evolution process when agents using M reach 3000,4000,4500,4900;
	public void resultInEvolution(){
		numberOfMStrategy=0; 
		
		for(int i=0;i<agentlist.size();i++){
          if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='M') numberOfMStrategy++;
		 }
		
		// System.out.println(" recorded3000  "+recorded3000);
		 
         if ((numberOfMStrategy>=3000)&(!recorded3000)){
			//System.out.println(" reach 3000");
			recorded3000=true;
	    	recordToFileMiddle();
         }
         
         if ((numberOfMStrategy>=4000)&(!recorded4000)){
			//System.out.println(" reach 4000");
			recorded4000=true;
	    	recordToFileMiddle();
         }
         
         if ((numberOfMStrategy>=4500)&(!recorded4500)){
			//System.out.println(" reach 4500");
			recorded4500=true;
	    	recordToFileMiddle();
         }
         
         if ((numberOfMStrategy>=4900)&(!recorded4900)){
			//System.out.println(" reach 4900");
			recorded4900=true;
	    	recordToFileMiddle();
         }
	}
	
	// record the result when agents using M reach 5000
    public void  recordToFile(){
         try {
			FileWriter  fwresult = new FileWriter("./SimulationResult.txt",true);
			BufferedWriter bwresult = new BufferedWriter(fwresult);
            PrintWriter pwresult= new PrintWriter(bwresult);
            
            numberOfLStrategy=0;
            numberOfMStrategy=0;
            numberOfHStrategy=0;
          
          Parameters p = RunEnvironment.getInstance().getParameters();
           int num = (Integer)p.getValue("number of agent");
          
          for(int i=0;i<agentlist.size();i++){
              if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='L') numberOfLStrategy++;
             else if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='M') numberOfMStrategy++;
              else numberOfHStrategy++;
             
//              totalPayoff+=((Agent) (agentlist.get(i))).getCurrentPayoff();
//              totalSocialPreferencePayoff+=((Agent) (agentlist.get(i))).getCurrentSocialPayoff();
        }
          
          int numberOfSocialAgent=(Integer) p.getValue("number of social preference agent");
          double alpha=(Double)p.getValue("alpha of social preference function");
          double beta=(Double)p.getValue("beta of social preference function");
         //double theta=(Double)p.getValue("theta of social preference coefficient");
          double wsProbability=(Double)p.getValue("WS probability");
          int degree=(Integer)p.getValue("neighbor size");
//          pwresult.print("       ");
          pwresult.printf("%.6f",((double)(numberOfLStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
          pwresult.print("    ");
          pwresult.printf("%.6f",((double)(numberOfMStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
          pwresult.print("     ");
          pwresult.printf("%.6f",((double)(numberOfHStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
          pwresult.print("     ");
          pwresult.printf("%d",numberOfMStrategy); 
          pwresult.print("     ");
//          pwresult.printf("%.1f",totalPayoff); 
//          pwresult.print("       ");
//          pwresult.printf("%.1f",totalSocialPreferencePayoff); 
//          pwresult.print("       ");
          pwresult.printf("%d",numberOfSocialAgent); 
          pwresult.print("     ");
          pwresult.printf("%d",degree); 
          pwresult.print("     ");
          pwresult.printf("%.4f",wsProbability); 
          pwresult.print("     ");
          pwresult.printf("%.4f",alpha); 
          pwresult.print("     ");
          pwresult.printf("%.4f",beta); 
          pwresult.print("     ");
          pwresult.printf("%.1f",(RunEnvironment.getInstance()).getCurrentSchedule().getTickCount()); 
          pwresult.println("   ");
          //pwresult.printf("%f",theta); 
          pwresult.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	     System.out.println("finished write to the file");
	     RunEnvironment.getInstance().endRun(); // end the game immediately. 
         }
    
	// record the milestone in the evolution process to a file 
    public void  recordToFileMiddle(){
        try {
			FileWriter  fwresult = new FileWriter("./SimulationResult.txt",true);
			BufferedWriter bwresult = new BufferedWriter(fwresult);
           PrintWriter pwresult= new PrintWriter(bwresult);
           
           numberOfLStrategy=0;
           numberOfMStrategy=0;
           numberOfHStrategy=0;
         
         Parameters p = RunEnvironment.getInstance().getParameters();
          int num = (Integer)p.getValue("number of agent");
         
         for(int i=0;i<agentlist.size();i++){
             if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='L') numberOfLStrategy++;
            else if(((Agent) (agentlist.get(i))).getCurrentStrategy()=='M') numberOfMStrategy++;
             else numberOfHStrategy++;
            
//             totalPayoff+=((Agent) (agentlist.get(i))).getCurrentPayoff();
//             totalSocialPreferencePayoff+=((Agent) (agentlist.get(i))).getCurrentSocialPayoff();
       }
         
         int numberOfSocialAgent=(Integer) p.getValue("number of social preference agent");
         double alpha=(Double)p.getValue("alpha of social preference function");
         double beta=(Double)p.getValue("beta of social preference function");
        //double theta=(Double)p.getValue("theta of social preference coefficient");
         double wsProbability=(Double)p.getValue("WS probability");
         int degree=(Integer)p.getValue("neighbor size");
//         pwresult.print("       ");
         pwresult.printf("%.6f",((double)(numberOfLStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
         pwresult.print("    ");
         pwresult.printf("%.6f",((double)(numberOfMStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
         pwresult.print("     ");
         pwresult.printf("%.6f",((double)(numberOfHStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
         pwresult.print("     ");
         pwresult.printf("%d",numberOfMStrategy); 
         pwresult.print("     ");
//         pwresult.printf("%.1f",totalPayoff); 
//         pwresult.print("       ");
//         pwresult.printf("%.1f",totalSocialPreferencePayoff); 
//         pwresult.print("       ");
         pwresult.printf("%d",numberOfSocialAgent); 
         pwresult.print("     ");
         pwresult.printf("%d",degree); 
         pwresult.print("     ");
         pwresult.printf("%.4f",wsProbability); 
         pwresult.print("     ");
         pwresult.printf("%.4f",alpha); 
         pwresult.print("     ");
         pwresult.printf("%.4f",beta); 
         pwresult.print("     ");
         pwresult.printf("%.1f",(RunEnvironment.getInstance()).getCurrentSchedule().getTickCount()); 
         pwresult.println("   ");
         //pwresult.printf("%f",theta); 
         pwresult.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	     //System.out.println("write to the file in the evolution process to record the milestone");
        }
	}