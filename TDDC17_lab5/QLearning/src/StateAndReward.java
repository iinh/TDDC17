
public class StateAndReward {

	public final static int ANGLE_STATES = 11; // Make sure this is odd, so (numberOfStates-1)/2 is an positive integer.
	public final static int VX_STATES = 3;
	public final static int VY_STATES = 5;
	
	
	
	/* State discretization function for the angle controller */
	public static String getStateAngle(double angle, double vx, double vy) {

		String state = Integer.toString(discretize(angle, ANGLE_STATES, (double)-Math.PI,
		(double)Math.PI));
		
		return state;
	}

	/* Reward function for the angle controller */
	public static double getRewardAngle(double angle, double vx, double vy) {

		double reward = 0;
		//double reward = 2*Math.PI - Math.abs(angle);	
		
		//String state = getStateAngle(angle, vx,vy);
		//reward = (numberOfStates-1)/2 - Math.abs(Integer.parseInt(state)-(numberOfStates-1)/2);
		
		int states_from_middle = Math.abs(getAngleState(angle) - ((ANGLE_STATES - 1) / 2));
				
		if(states_from_middle == 0){
			reward = 55;
		}
		else if (states_from_middle == 1){
			reward = 25;
		}
		else if(states_from_middle == 2){
			reward = 12;
		}
		else{
			reward = 1;
		}
		
		
		return reward;
		
	}

	/* State discretization function for the full hover controller */
	public static String getStateHover(double angle, double vx, double vy) {

		String state = "Angle: "+getAngleState(angle)+"VX: "+getVxState(vx)+"Vy: "+getVyState(vy) ;
		
		return state;
	}

	/* Reward function for the full hover controller */
	public static double getRewardHover(double angle, double vx, double vy) {
		
		double reward = getRewardAngle(angle,vx,vy)+getVXReward(vx)+getVYReward(vy);

		return reward;
	}
	
	public static double getVXReward(double vx) {
		double reward = 0;
		int states_from_middle = Math.abs(getVxState(vx) -  ((VX_STATES - 1) / 2));
		
		if(states_from_middle == 0){
			reward = 15;

		}
		else{
			reward = 4;
		}
		
		return reward;
	}
	
	public static double getVYReward(double vy) {
		double reward = 0;
		int states_from_middle = Math.abs(getVyState(vy) -  ((VY_STATES - 1) / 2)); 
		
		if(states_from_middle == 0){
			reward = 30;

		}
		else if (states_from_middle == 1){
			reward = 8;
		}
		else{
			reward = 1;
		}
		
		return reward;
	}
	
	public static int getAngleState(double angle) {
		return discretize(angle, ANGLE_STATES, -2, 2);
	}
	
	public static int getVxState(double vx) {
		return discretize(vx, VX_STATES, -3, 3);
	}
	
	public static int getVyState(double vy) {
		return discretize(vy, VY_STATES, -5, 5);
	}

	// ///////////////////////////////////////////////////////////
	// discretize() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 1 and nrValues-2 is returned.
	//
	// Use discretize2() if you want a discretization method that does
	// not handle values lower than min and higher than max.
	// ///////////////////////////////////////////////////////////
	public static int discretize(double value, int nrValues, double min,
			double max) {
		if (nrValues < 2) {
			return 0;
		}

		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * (nrValues - 2)) + 1;
	}

	// ///////////////////////////////////////////////////////////
	// discretize2() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 0 and nrValues-1 is returned.
	// ///////////////////////////////////////////////////////////
	public static int discretize2(double value, int nrValues, double min,
			double max) {
		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * nrValues);
	}

}
