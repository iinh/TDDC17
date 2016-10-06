


public class StateAndReward {

	public final static int numberOfStates = 10; // Make sure this is odd, so (numberOfStates-1)/2 is an positive integer.
	
	
	/* State discretization function for the angle controller */
	public static String getStateAngle(double angle, double vx, double vy) {

		String state = Integer.toString(discretize(angle, numberOfStates, (double)-Math.PI,
		(double)Math.PI));
		
		return state;
	}

	/* State discretization function for the angle controller */
	public static String getStateVx(double angle, double vx, double vy) {

		String state = Integer.toString(discretize(vx, numberOfStates, -10, 10));
		
		return state;
	}
	
	/* State discretization function for the angle controller */
	public static String getStateVy(double angle, double vx, double vy) {

		String state = Integer.toString(discretize(vy, numberOfStates/2, -5, 5));
		
		return state;
	}


	
	/* Reward function for the angle controller */
	public static double getRewardAngle(double angle, double vx, double vy) {

		
		double reward = Math.pow(Math.PI,6) - Math.abs(Math.pow(angle,6));	
		
		return reward;
		
	}
	
	public static double getRewardVx(double angle, double vx, double vy){
		double reward  = 0;
		
		reward = Math.pow(10,2)-Math.abs(Math.pow(vx,2));
		
		return reward;
		
	}

	public static double getRewardVy(double angle, double vx, double vy){
		double reward  = 0;
		
		reward = Math.pow(10,3)-Math.abs(Math.pow(vy,3));
		
		return reward;
		
	}	
	
	/* State discretization function for the full hover controller */
	public static String getStateHover(double angle, double vx, double vy) {

		/* TODO: IMPLEMENT THIS FUNCTION */
		// -10 < vx < 10	
		// ^vänster   ^höger
		
		// -4.5 < vy < 15.5
		//  ^upp       ^ner
		String angle_state = Integer.toString(discretize(angle, numberOfStates*4, (double)-Math.PI,
				(double)Math.PI));
		
		String vx_state = Integer.toString(discretize(vx, numberOfStates, -10, 10));
		
		String vy_state = Integer.toString(discretize(vy, numberOfStates/2, -5, 16));
		
		
		String state = angle_state + vx_state + vy_state;
		
		return state;
	}

	/* Reward function for the full hover controller */
	public static double getRewardHover(double angle, double vx, double vy) {

		/* TODO: IMPLEMENT THIS FUNCTION */
		
		double reward = 0;
		reward = 50*getRewardAngle(angle,vx,vy) + 5*getRewardVx(angle, vx, vy) + 3*getRewardVy(angle, vx, vy);

		return reward;
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
