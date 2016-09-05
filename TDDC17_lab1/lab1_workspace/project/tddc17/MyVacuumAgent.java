package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import tddc17.MyAgentState.Node;



class MyAgentState
{
	public static class Node {
		int x;
		int y;
		ArrayList<Node> neighbors;
		Node previous;
		boolean visited = false;
		
		Node(int x, int y, Node previous){
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

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();



	// Here you can define your variables!
	//public int iterationCounter = 10;


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
	public LinkedList<Node> updatePath(Node goalNode, Node rootNode){
		Queue<Node> q = new LinkedList<Node>();
		Node[][] worldNodes = initWorldMap();
		LinkedList<Node> path = new LinkedList<Node>();
		q.add(rootNode);
		rootNode.previous = null;

		while(!q.isEmpty()){
			Node currentNode = q.remove();
			if(currentNode.x == goalNode.x && currentNode.y == goalNode.y){
				while(currentNode.previous != null){
					path.add(currentNode);
					currentNode = currentNode.previous;
				}
				return path;
			}
			
			else{
				currentNode.visited = true;
				if(state.world[currentNode.x][currentNode.y-1]!= state.WALL || !worldNodes[currentNode.x][currentNode.y-1].visited){
					q.add(new Node(currentNode.x, currentNode.y-1, currentNode));
				}
				
				if(state.world[currentNode.x+1][currentNode.y]!= state.WALL || !worldNodes[currentNode.x+1][currentNode.y].visited){
					q.add(new Node(currentNode.x+1, currentNode.y, currentNode));
				}
				
				if(state.world[currentNode.x][currentNode.y+1]!= state.WALL || !worldNodes[currentNode.x][currentNode.y+1].visited){
					q.add(new Node(currentNode.x, currentNode.y+1, currentNode));
				}
				
				if(state.world[currentNode.x-1][currentNode.y]!= state.WALL || !worldNodes[currentNode.x-1][currentNode.y].visited){
					q.add(new Node(currentNode.x-1, currentNode.y, currentNode));
				}
			}
		}
		return path;

		//	    /* Breadth First Search */
		//	    vector<Node *> breadthFirstSearch(BasicGraph& graph, Vertex* start, Vertex* end) {
		//	        graph.resetData();
		//	        queue<Node*> q;
		//	        q.push(start);
		//	        vector<Vertex*> path;
		//	        while (!q.empty()){
		//	            Node* t = q.front();
		//	            t->visited = true;
		//	            t->setColor(GREEN);
		//	            if (t == end){ //Found a path
		//	                while(t != NULL){
		//	                    path.push_back(t);
		//	                    t = t->previous;
		//	                }
		//	                return path;
		//	            }
		//	            q.pop();
		//	            for (Node *node : graph.getNeighbors(t)){
		//	                if(!node->visited){
		//	                    node->previous = t;
		//	                    q.push(node);
		//	                    node->setColor(YELLOW);
		//	                }
		//	            }
		//	        }
		//	        return path;
		//	    }


		//	    public List search(Node startNode, Node goalNode) {
		//	    	  // list of visited nodes
		//	    	  LinkedList closedList = new LinkedList();
		//	    	  
		//	    	  // list of nodes to visit (sorted)
		//	    	  LinkedList openList = new LinkedList();
		//	    	  openList.add(startNode);
		//	    	  startNode.pathParent = null;
		//	    	  
		//	    	  while (!openList.isEmpty()) {
		//	    	    Node node = (Node)openList.removeFirst();
		//	    	    if (node == goalNode) {
		//	    	      // path found!
		//	    	      return constructPath(goalNode);
		//	    	    }
		//	    	    else {
		//	    	      closedList.add(node);
		//	    	      
		//	    	      // add neighbors to the open list
		//	    	      Iterator i = node.neighbors.iterator();
		//	    	      while (i.hasNext()) {
		//	    	        Node neighborNode = (Node)i.next();
		//	    	        if (!closedList.contains(neighborNode) &&
		//	    	          !openList.contains(neighborNode)) 
		//	    	        {
		//	    	          neighborNode.pathParent = node;
		//	    	          openList.add(neighborNode);
		//	    	        }
		//	    	      }
		//	    	    }
		//	    	  }
		//	    	  
		//	    	  // no path found
		//	    	  return null;
		//	    	}

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


		return null;
	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
