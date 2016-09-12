package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import tddc17.MyAgentState.Node;



class MyAgentState
{
	
	// Node class
	public static class Node {
		int x;
		int y;
		ArrayList<Node> neighbors;
		Node previous;
		boolean visited = false;

		Node(int x, int y, Node previous){
			this.visited = false;
			this.x = x;
			this.y = y;
			this.previous = previous;
		}
	}

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

	MyAgentState()
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
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
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
					System.out.print(" _ ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();
	private int xCount = 2; // next coordinates to explore
	private int yCount = 1; // --
	private boolean goingEast = true;
	private boolean doneCleaning = false;
	private boolean startedHomeJourney = false;



	// Here you can define your variables!
	//public int iterationCounter = 10;
	LinkedList<Node> path = new LinkedList<Node>();


	public MyAgentState state = new MyAgentState();

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

	// Fill worldNodes with new Nodes at all coordinated of the map that are unvisited.
	public Node[][]  initWorldMap(){
		Node[][] worldNodes = new Node[30][30];
		for (int i=0; i < state.world.length; i++)
		{
			for (int j=0; j < state.world[i].length ; j++)
			{
				worldNodes[i][j]= new Node(i,j,null);
			}

		}

		return worldNodes;
	}

	
	public boolean outOfBounds(int x, int y){
		return !(1 <= x &&  x <= 15 && 1 <= y &&  y <= 15); 
	}
	
	// Returns true if we're facing the correct direction, depening on next goal position.
	public boolean correctDirection(int goalX, int goalY){
		if(state.agent_x_position > goalX && state.agent_direction != state.WEST){
			return false;
		}
		else if(state.agent_x_position < goalX && state.agent_direction != state.EAST){
			return false;

		}
		else if(state.agent_y_position > goalY && state.agent_direction != state.NORTH){
			return false;

		}
		else if(state.agent_y_position < goalY && state.agent_direction != state.SOUTH){
			return false;
		}
		else{
			return true;
		}
	}
	
	// Update our next goal coordinates.
	public void updateGoal(){
		if(goingEast){
			xCount++;
			if(xCount>15){
				goingEast = false;
				xCount = 15;
				yCount++;
				if(yCount>15){
					doneCleaning = true;
					yCount = 1;
				}
			}	
		}
		else{
			xCount--;
			if(xCount<1){
				xCount = 1;
				goingEast = true;
				yCount++;
				if(yCount>15){
					doneCleaning = true;
					yCount = 1;
				}
			}
		}
		
	}
	
	// Main search algorithm using Breadth First Search. Returns path to next node.
	public LinkedList<Node> updatePath(Node goalNode, Node rootNode){
		Queue<Node> q = new LinkedList<Node>();
		Node[][] worldNodes = initWorldMap();
		LinkedList<Node> path = new LinkedList<Node>();
		q.add(rootNode);
		rootNode.previous = null;

		while(!q.isEmpty()){
			Node currentNode = q.peek();
			if(currentNode.x == goalNode.x && currentNode.y == goalNode.y){
				while(currentNode.previous != null){
					path.add(currentNode);
					currentNode = currentNode.previous;
				}
				return path;
			}


			q.remove();
			currentNode.visited = true;
			worldNodes[currentNode.x][currentNode.y].visited = true;
			if(!outOfBounds(currentNode.x , currentNode.y-1) && (state.world[currentNode.x][currentNode.y-1]!= state.WALL && !worldNodes[currentNode.x][currentNode.y-1].visited)){
				q.add(new Node(currentNode.x, currentNode.y-1, currentNode));
			}

			if(!outOfBounds(currentNode.x+1 , currentNode.y) && (state.world[currentNode.x+1][currentNode.y]!= state.WALL && !worldNodes[currentNode.x+1][currentNode.y].visited)){
				q.add(new Node(currentNode.x+1, currentNode.y, currentNode));
			}

			if(!outOfBounds(currentNode.x , currentNode.y+1) && (state.world[currentNode.x][currentNode.y+1]!= state.WALL && !worldNodes[currentNode.x][currentNode.y+1].visited)){
				q.add(new Node(currentNode.x, currentNode.y+1, currentNode));
			}

			if(!outOfBounds(currentNode.x-1 , currentNode.y) && (state.world[currentNode.x-1][currentNode.y]!= state.WALL && !worldNodes[currentNode.x-1][currentNode.y].visited)){
				q.add(new Node(currentNode.x-1, currentNode.y, currentNode));
			}

		}
		return path;

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
		state.printWorldDebug();
		state.updatePosition(p);

		// Go to the start position after random moves.
		if(!home && !startedHomeJourney){
			path = updatePath(new Node(1, 1, null), new Node(state.agent_x_position, state.agent_y_position, null));
			startedHomeJourney = true;
		}
		
		// Done cleaning, and we're home.
		else if(home && doneCleaning){
			return NoOpAction.NO_OP;
		}

		else if (dirt){
			state.agent_last_action=state.ACTION_SUCK;
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
			return LIUVacuumEnvironment.ACTION_SUCK;
		}
		
		else if(bump){
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
			
			// We found a bump, and have to calculate a new path to the goal (located at (xCount, yCount))
			path = updatePath(new Node(xCount, yCount, null), new Node(state.agent_x_position, state.agent_y_position, null));
			
			// In case the new goal is not possible, find a new one until one is possible.
			while(path.isEmpty() || state.world[xCount][yCount] == state.WALL){
				updateGoal();
				path = updatePath(new Node(xCount, yCount, null), new Node(state.agent_x_position, state.agent_y_position, null));
			}


		}
		
		// Path is empty means we are at our goal -> find path to a new goal.
		else if(path.isEmpty()){
			if(xCount == state.agent_x_position && yCount == state.agent_y_position){
				updateGoal();
			}
		
			// In case path is not possible, find a new one.
			while(path.isEmpty()){
				path = updatePath(new Node(xCount, yCount, null), new Node(state.agent_x_position, state.agent_y_position, null));
				updateGoal();
			}
		}
		
		// We keep turning left until we're facing the correct direction depending on our next goal.
		if(!correctDirection(path.peekLast().x, path.peekLast().y)){
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0) 
				state.agent_direction +=4;
			state.agent_last_action=state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		}

		
		// If nothing above triggered, we're in the clear to move forward.
		else{
			if(!bump) {
				path.removeLast();
				state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);				
			}
			
			state.agent_last_action=state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		}

	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
