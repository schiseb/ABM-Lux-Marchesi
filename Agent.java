import java.util.ArrayList;

public class Agent {

	private State state;

	private double cash = 0;
	private double holding = 0;
	// Counters for the composition of traders.
	private static int fcount = 0;
	private static int pcount = 0;
	private static int ocount = 0;
	private static int ccount = 0;
	// Contains the information about the current state of the agent.
	private ArrayList<State> statelist = new ArrayList<State>();
	private ArrayList<Double> chist = new ArrayList<Double>();
	private ArrayList<Double> hhist = new ArrayList<Double>();

	public Agent(State state) {
		this.setState(state);
	}

	// *********************************
	// Returns the initial Chartist count as assigned during test.initialize()
	public static int getCcountInit() {
		return ccount;
	}

	// Returns the sum of optimists and pessimists = chartists
	public static int getCcount() {
		return Agent.ocount + Agent.pcount;
	}

	public static void setCcount(int ccount) {
		Agent.ccount = ccount;
	}

	// *********************************
	public static int getFcount() {
		return fcount;
	}

	public static void setFcount(int fcount) {
		Agent.fcount = fcount;
	}

	public static void addFcount() {
		Agent.fcount++;
	}

	public static void dropFcount() {
		Agent.fcount--;
	}

	// *********************************
	public static int getPcount() {
		return pcount;
	}

	public static void setPcount(int pcount) {
		Agent.pcount = pcount;
	}

	public static void addPcount() {
		Agent.pcount++;
	}

	public static void dropPcount() {
		Agent.pcount--;
	}

	// *********************************
	public static int getOcount() {
		return ocount;
	}

	public static void setOcount(int ocount) {
		Agent.ocount = ocount;
	}

	public static void addOcount() {
		Agent.ocount++;
	}

	public static void dropOcount() {
		Agent.ocount--;
	}

	// *********************************
	public State getState() {
		return this.state;
	}

	public void setState(State state) {
		this.state = state;

	}

	public void setStateList(State state) {
		this.statelist.add(state);
	}

	public String getStateList() {
		return statelist.toString();
	}

	// *********************************
	public String getCash() {
		return chist.toString();
	}

	public void setCash(double cash) {
		this.cash -= cash;
		this.chist.add(this.cash);
	}

	public String getHolding() {
		return hhist.toString();
	}

	public void setHolding(double holding) {
		this.holding += holding;
		this.hhist.add(this.holding);
	}

	// *********************************
	public static String getAgentsCount() {
		String s = getFcount() + " " + getOcount() + " " + getPcount();
		return s;
	}

	public static void reset() {
		Agent.setFcount(0);
		Agent.setOcount(0);
		Agent.setPcount(0);
	}
}
