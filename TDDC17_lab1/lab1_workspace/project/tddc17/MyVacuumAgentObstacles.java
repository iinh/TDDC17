package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Random;

class MyAgentStateObstacles
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
	
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;
	
	MyAgentStateObstacles()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentStateObstacles.NORTH:
				agent_y_position--;
				break;
			case MyAgentStateObstacles.EAST:
				agent_x_position++;
				break;
			case MyAgentStateObstacles.SOUTH:
				agent_y_position++;
				break;
			case MyAgentStateObstacles.WEST:
				agent_x_position--;
				break;
			}
	    }
	}
	
	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}
	
	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgramObstacles implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();
	
	
	// Here you can define your variables!
	//public int iterationCounter = 10;
	private boolean foundStartingPoint = false;
	private boolean movedDown = false;
	private boolean turningRightNext = true;
	private boolean turning = false;
	private boolean doneCleaning = false;
	
	public MyAgentStateObstacles state = new MyAgentStateObstacles();
	
	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
	
	
	@Override
	public Action execute(Percept percept) {
		
		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}
		
    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!
    	    	
    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);
    	 	
	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);
	    
	   
	    state.updatePosition(p);
	    
	    /* We will go from left to right and zig-zag through 
	     * the entire map until we're done,
	     * then go home.
	     */

	    // Finished condition
	    if(doneCleaning && home){
	    	System.out.println("Done cleaning!");
	    	state.printWorldDebug();
	    	state.agent_last_action=state.ACTION_NONE;
	    	return NoOpAction.NO_OP;
	    }
	    
	    // Either we haven't gone to the starting point yet, or we're done and should go home.
	    else if(!foundStartingPoint || (doneCleaning && !home)){
	    	if(home && state.agent_direction == MyAgentStateObstacles.EAST){
	    		foundStartingPoint = true;
	    		state.agent_last_action=state.ACTION_SUCK;
		    	return LIUVacuumEnvironment.ACTION_SUCK;
	    	}
	    	
	    	if(state.agent_y_position != 1 && state.agent_direction != MyAgentStateObstacles.NORTH){
			    state.agent_direction = ((state.agent_direction-1) % 4);
			    if (state.agent_direction<0) 
			    	state.agent_direction +=4;
			    state.agent_last_action = state.ACTION_TURN_LEFT;
	    		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
	    	}
	    	else if(state.agent_y_position != 1){
	    		state.agent_last_action=state.ACTION_MOVE_FORWARD;
	    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	    	}
	    	
	    	else if(state.agent_x_position != 1 && state.agent_direction != MyAgentStateObstacles.WEST){
	    		state.agent_direction = ((state.agent_direction-1) % 4);
			    if (state.agent_direction<0){
			    	state.agent_direction +=4;
			    }
			    state.agent_last_action = state.ACTION_TURN_LEFT;
	    		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
	    	}
	    	else if(state.agent_x_position != 1){
	    		state.agent_last_action=state.ACTION_MOVE_FORWARD;
	    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	    	}
	    
	    	// Also, turn east to prepare for the starting movement.
		    if(state.agent_direction != MyAgentStateObstacles.EAST){
		    	state.agent_direction = ((state.agent_direction-1) % 4);
			    if (state.agent_direction<0) 
			    	state.agent_direction +=4;
			    state.agent_last_action = state.ACTION_TURN_LEFT;
				return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				
		    }
	    }
	    
	    // Clean dirt if we find some
	    else if(dirt){
	    	state.agent_last_action=state.ACTION_SUCK;
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    }
	    
	    // If bump we'll initiate the 180 turn starting either with left or right depending on the current direction. 
	    else if(bump){
	    	turning = true;
	    	movedDown = false;
	    	if(state.agent_direction == MyAgentStateObstacles.EAST){
	    		state.agent_direction = ((state.agent_direction+1) % 4);
			    state.agent_last_action = state.ACTION_TURN_RIGHT;
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;	
	    	}
	    	
	    	else if(state.agent_direction == MyAgentStateObstacles.WEST){
	    		state.agent_direction = ((state.agent_direction-1) % 4);
	    		if (state.agent_direction<0) 
			    	state.agent_direction +=4;
			    state.agent_last_action = state.ACTION_TURN_LEFT;
				return LIUVacuumEnvironment.ACTION_TURN_LEFT;	
	    		
	    	}
	    	
	    	// If we bump while not looking EAST or WEST, 
	    	// it means we have reached the bottom and finished cleaning.
	    	else{
	    		doneCleaning = true;
	    		state.agent_last_action=state.ACTION_SUCK;
		    	return LIUVacuumEnvironment.ACTION_SUCK;
	    	}		
	    }
	    
	    // If we're in the middle of a turn and
	    // we should move down one step, provided that we haven't already.
	    else if(state.agent_direction == MyAgentStateObstacles.SOUTH && !movedDown && turning){
	    	movedDown = true;
	    	state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;	
	    }
	    
	    // Complete the 180 degrees turn by turning the same direction as before.
	    else if(state.agent_direction != MyAgentStateObstacles.WEST && turning && turningRightNext){
	    	turningRightNext = false;
	    	turning = false;
	    	state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;	
	    }
	    
	    else if(state.agent_direction != MyAgentStateObstacles.EAST && turning && !turningRightNext){
	    	turningRightNext = true;
	    	turning = false;
	    	state.agent_direction = ((state.agent_direction-1) % 4);
    		if (state.agent_direction<0) 
		    	state.agent_direction +=4;
	    	state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;	
	    }
	    
	    
	    // Nothing to do, move forward.
	    else{
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
	    	state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;	
	    	
	    }
	    
	    // Somehow, we failed. This should never happen.
	    state.agent_last_action=state.ACTION_NONE;
	    System.out.print("Failed");
	    return NoOpAction.NO_OP;
	}
}
	
public class MyVacuumAgentObstacles extends AbstractAgent {
    public MyVacuumAgentObstacles() {
    	super(new MyAgentProgramObstacles());
	}
}
