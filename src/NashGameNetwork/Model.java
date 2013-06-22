package NashGameNetwork;

// the model, in charge of the creation of the agent, the world and the execution of the program. 
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
  public class Model extends DefaultContext implements ContextBuilder<Agent>{
  //Here we will first construct the world which the agent lives in it.
	//first creates the context, then add the agent 
	//then creates a small world complex network
	//after the agents' complex interaction network is done,
	//the model will random schedule the agent's step method
	private ArrayList agentlist= new ArrayList();
	private int actionNumber;
	private final  int stop=5000;
	private double  totalPayoff;
	private double  totalSocialPreferencePayoff;
	private int numberOfLStrategy;
	private int numberOfMStrategy;
	private int numberOfHStrategy;
	
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
        System.out.println(" the network degree    "+net.getDegree());
        
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
		
		while(sum<numSocialPreference){
		    RandomHelper.createUniform();
			int random=RandomHelper.nextIntFromTo(0,numSocialPreference -1);
		    if (!((Agent)agentlist.get(random)).isIfSocialPreference()){
		    ((Agent)agentlist.get(random)).setIfSocialPreference(true);
		    sum++;
		    }
		}
		  
		System.out.println("set the agent which is the social preference type  is done!!!");
		System.out.println("Model will begin");
		
		return context;
	  }

@ScheduledMethod(start=stop)
	public void end(){
		try {
			FileWriter  fwresult = new FileWriter("./SimulationDataOne.txt",true);
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
                
              totalPayoff+=((Agent) (agentlist.get(i))).getCurrentPayoff();
              totalSocialPreferencePayoff+=((Agent) (agentlist.get(i))).getCurrentSocialPayoff();
          }
          
          int numberOfSocialAgent=(Integer) p.getValue("number of social preference agent");
          double alpha=(Double)p.getValue("alpha of social preference function");
          double beta=(Double)p.getValue("beta of social preference function");
         //double theta=(Double)p.getValue("theta of social preference coefficient");
          double wsProbability=(Double)p.getValue("WS probability");
          int degree=(Integer)p.getValue("neighbor size");
          pwresult.print("       ");
          pwresult.printf("%.15f",((double)(numberOfLStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
          pwresult.print("       ");
          pwresult.printf("%.15f",((double)(numberOfMStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
          pwresult.print("       ");
          pwresult.printf("%.15f",((double)(numberOfHStrategy)/(numberOfLStrategy+numberOfMStrategy+numberOfHStrategy)));
          pwresult.print("       ");
          pwresult.printf("%.1f",(RunEnvironment.getInstance()).getCurrentSchedule().getTickCount()); 
          pwresult.print("       ");
          pwresult.printf("%.1f",totalPayoff); 
          pwresult.print("       ");
          pwresult.printf("%.1f",totalSocialPreferencePayoff); 
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
          //pwresult.printf("%f",theta); 
          pwresult.println("       ");
          pwresult.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	   System.out.println("finished write to the file");
		(RunEnvironment.getInstance()).endAt(stop);
	}
}
