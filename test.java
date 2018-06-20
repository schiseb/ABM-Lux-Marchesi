
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

public class test {

	private double price = 10.0;
	private int obs = 1000;
	private int agents = 500;
	private double interest = 0.0004;
	private double dividend = 0.004;
	private double v1 = 3.0;
	private double v2 = 2.0;
	private double a1 = 0.6;
	private double a2 = 0.2;
	private double a3 = 0.5;
	private double discount = 0.75;
	private double sigma = 0.05;
	private double tvc = 10.0;
	private double tvf = 5.0;
	private double beta = 6.0;

	private static String delim = "    "; // 4

	// ******************************************
	private Random r = new Random();
	/** Number of micro-intervals per unit time steps */
	private int steps = 500;
	private int plag = 100;

	private double chart_max = 1.0;
	private double pf = price; // Fundamental price
	private double pdot = 0.0;
	private double p_adj = 0.0;

	// Containers for all relevant variables
	private ArrayList<Double> pricefu_hist = new ArrayList<Double>();
	private ArrayList<Double> price_hist = new ArrayList<Double>();
	private ArrayList<Double> price_temp;
	private ArrayList<Double> return_hist = new ArrayList<Double>();
	private ArrayList<Double> chart_index_hist = new ArrayList<Double>();
	private ArrayList<Double> opinion_index_hist = new ArrayList<Double>();
	private ArrayList<Double> chart_wealth_hist = new ArrayList<Double>();
	private ArrayList<Double> fund_wealth_hist = new ArrayList<Double>();

	// Minimum amount of traders in each strategy
	private int agents_min = 4;
	// Total number of chartists
	private int agents_chart = 2 * agents_min;
	// Total number of fundamentalists
	private int agents_fund = agents - agents_chart;
	// Number of agents who are optimistic
	private int chart_opt = agents_min;
	// Number of agents who are pessimistic
	private int chart_pes = agents_min;

	// Excess demand
	private double edc = (chart_opt - chart_pes) * (tvc / (double) agents);
	private double edf = agents_fund * (tvf / (double) agents) * (pf - price);
	private double edt = edc + edf;

	// Stock holdings and costs for purchasing
	double chart_hold = 0;
	double fund_hold = 0;
	double chart_mon = 0;
	double fund_mon = 0;

	private double chart_index = (double) agents_chart / agents;
	private double opinion_index = (double) (chart_opt - chart_pes) / agents_chart;

	// Transition probabilities and profits
	private double u1 = a1 * opinion_index + a2 * (pdot / v1);
	private double chart_profit = (dividend + pdot / v2) / price;
	private double fund_profit = discount * Math.abs((pf - price) / price);
	private double u2 = a3 * (chart_profit - interest - fund_profit);
	private double u3 = a3 * (interest - chart_profit - fund_profit);

	private double p_to_o = v1 * (((double) agents_chart / agents) * Math.exp(u1));
	private double o_to_p = v1 * (((double) agents_chart / agents) * Math.exp(-u1));

	private double f_to_o = v2 * (((double) chart_opt / agents) * Math.exp(u2));
	private double o_to_f = v2 * (((double) agents_fund / agents) * Math.exp(-u2));

	private double f_to_p = v2 * (((double) chart_pes / agents) * Math.exp(u3));
	private double p_to_f = v2 * (((double) agents_fund / agents) * Math.exp(-u3));

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test t = new test();
		// t.initSimulation();
		t.simulate();

	}

	private ArrayList<Agent> agentslist = new ArrayList<Agent>();

	private void initSimulation() {

		price_hist.clear();
		pricefu_hist.clear();
		return_hist.clear();
		chart_index_hist.clear();
		opinion_index_hist.clear();
		chart_wealth_hist.clear();
		fund_wealth_hist.clear();
		agentslist.clear();
		Agent.reset();

		pf = price;
		pdot = 0;

		// Calculating the upper threshold of chartists, see zmax.pdf and Lux-Marchesi
		// (2000, p. 686)
		double a = 2 * tvc * beta * a2 - (2 * tvc * beta * a3) / pf;
		double b = tvf * beta - 2 * v1 + 2 * a1 * v1 + (2 * tvc * beta * a3) / pf;
		double c = -tvf * beta;

		// 0.6597070860464694
		chart_max = (-b + Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);

		// Randomly distributing chartists/fundamentalists
		Agent.setCcount(Math.max((int) (r.nextDouble() * chart_max * agents), 2 * agents_min));
		Agent.setFcount(agents - Agent.getCcountInit());

		Agent.setOcount(Math.min(Math.max((int) (r.nextDouble() * Agent.getCcountInit()), agents_min),
				Agent.getCcountInit() - agents_min));
		Agent.setPcount(Agent.getCcountInit() - Agent.getOcount());

		for (int i = 0; i < Agent.getFcount(); i++) {
			agentslist.add(new Agent(State.FUNDAMENTALIST));
		}
		for (int i = 0; i < Agent.getOcount(); i++) {
			agentslist.add(new Agent(State.OPTIMIST));
		}
		for (int i = 0; i < Agent.getPcount(); i++) {
			agentslist.add(new Agent(State.PESSIMIST));
		}

		// Initialization of variables and arrays
		chart_index = (double) (Agent.getCcount()) / agents;
		opinion_index = (double) (Agent.getOcount() - Agent.getPcount()) / (Agent.getCcount());

		// System.out.println(Agent.getAgentsCount() + " " + chart_index + " " +
		// opinion_index);

		price_temp = new ArrayList<Double>(Collections.nCopies(plag, price));
		price_hist.add(pf);
		pricefu_hist.add(pf);
		return_hist.add(0.0);
		chart_index_hist.add(chart_index);
		opinion_index_hist.add(opinion_index);
		chart_wealth_hist.add(0.0);
		fund_wealth_hist.add(0.0);
		// System.out.println(String.format(Locale.ENGLISH, "%04d", 1) + delim
		// + String.format(Locale.ENGLISH, "%05.2f", price_hist.get(0)) + delim
		// + String.format(Locale.ENGLISH, "%.3f", return_hist.get(0)) + delim
		// + String.format(Locale.ENGLISH, "%.3f", chart_index_hist.get(0)) + delim
		// + String.format(Locale.ENGLISH, "%.3f", opinion_index_hist.get(0)) + "\n");

	}

	private void simulate() {
		long start = System.currentTimeMillis();

		initSimulation();

		for (int i = 0; i < obs; i++) {

			for (int j = 0; j < steps; j++) {

				// Transition probabilities
				u1 = a1 * opinion_index + a2 * (pdot / v1);

				chart_profit = (dividend + pdot / v2) / price;
				fund_profit = discount * Math.abs((pf - price) / price);
				u2 = a3 * (chart_profit - interest - fund_profit);
				u3 = a3 * (interest - chart_profit - fund_profit);
				// Changing strategies
				/*
				 * The change happens as follows: The for-loop iterates through the whole
				 * agents-list. For every Agent a, who has his own history of states, the last
				 * known state is checked. Depending on the state of the agent, he randomly
				 * meets another trader from a different group. In case the randomly generated
				 * number is smaller than the transition probability AND there are at least
				 * agents_min traders in each group, he either switches or stays in the same
				 * state. Furthermore the upper limit chart_max is checked, if there are more
				 * chartists than chart_max, agents are not allowed to switch to a chartist
				 * group.
				 */

				for (Agent a : agentslist) {
					double rnd = r.nextDouble();
					boolean bool = r.nextBoolean();

					switch (a.getState()) {
					case FUNDAMENTALIST:
						if (bool) {
							f_to_o = v2 * (((double) Agent.getOcount() / agents) * Math.exp(u2)) / steps;
							if (rnd < f_to_o && Agent.getFcount() > agents_min
									&& ((Agent.getCcount()) < (agents * chart_max))) {
								a.setState(State.OPTIMIST);
								Agent.addOcount();
								Agent.dropFcount();
							} else {
								a.setState(State.FUNDAMENTALIST);
							}
						} else {
							f_to_p = v2 * (((double) Agent.getPcount() / agents) * Math.exp(u3)) / steps;
							if (rnd < f_to_p && Agent.getFcount() > agents_min
									&& ((Agent.getCcount()) < (agents * chart_max))) {
								a.setState(State.PESSIMIST);
								Agent.addPcount();
								Agent.dropFcount();
							} else {
								a.setState(State.FUNDAMENTALIST);
							}
						}
						break;
					case OPTIMIST:
						if (bool) {
							o_to_p = v1 * (((double) Agent.getCcount() / agents) * Math.exp(-u1)) / steps;
							if (rnd < o_to_p && Agent.getOcount() > agents_min) {
								a.setState(State.PESSIMIST);
								Agent.addPcount();
								Agent.dropOcount();
							} else {
								a.setState(State.OPTIMIST);
							}
						} else {
							o_to_f = v2 * (((double) Agent.getFcount() / agents) * Math.exp(-u2)) / steps;
							if (rnd < o_to_f && Agent.getOcount() > agents_min) {
								a.setState(State.FUNDAMENTALIST);
								Agent.addFcount();
								Agent.dropOcount();
							} else {
								a.setState(State.OPTIMIST);
							}
						}
						break;
					case PESSIMIST:
						if (bool) {
							p_to_o = v1 * (((double) Agent.getCcount() / agents) * Math.exp(u1)) / steps;
							if (rnd < p_to_o && Agent.getPcount() > agents_min) {
								a.setState(State.OPTIMIST);
								Agent.addOcount();
								Agent.dropPcount();
							} else {
								a.setState(State.PESSIMIST);
							}
						} else {
							p_to_f = v2 * (((double) Agent.getFcount() / agents) * Math.exp(-u3)) / steps;
							if (rnd < p_to_f && Agent.getPcount() > agents_min) {
								a.setState(State.FUNDAMENTALIST);
								Agent.addFcount();
								Agent.dropPcount();
							} else {
								a.setState(State.PESSIMIST);
							}
						}
						break;
					default:
						break;

					}
				} // End of Agent-for-Loop
					// Excess Demand and Price adjustments
				edc = ((double) Agent.getOcount() - Agent.getPcount()) * (tvc / agents);
				edf = (double) Agent.getFcount() * (tvf / agents) * (pf - price);
				edt = edc + edf;

				chart_hold += edc;
				fund_hold += edf;

				p_adj = beta * (edt + sigma * r.nextGaussian()) * 100 / (double) steps;
				price = price + Math.signum(p_adj) * (r.nextDouble() > Math.abs(p_adj) ? 0 : 1) / 100;
				pdot = (price - price_temp.get(0)) / ((double) plag / steps);
				price_temp = new ArrayList<>(price_temp.subList(1, price_temp.size()));
				price_temp.add(price);

				chart_mon -= edc * price;
				fund_mon -= edf * price;

			} // End of Inner-Loop (500 microsteps)
			for (

			Agent a : agentslist) {
				if (a.getState() == State.FUNDAMENTALIST) {
					a.setHolding(fund_hold);
					a.setCash(fund_mon);
				} else {
					a.setHolding(chart_hold);
					a.setCash(chart_mon);
				}
				a.setStateList(a.getState());
			}
			// Update opinion index and chartist index
			chart_index = (double) Agent.getCcount() / agents;
			opinion_index = (double) (Agent.getOcount() - Agent.getPcount()) / Agent.getCcount();

			pricefu_hist.add(pf);
			price_hist.add(price);
			return_hist.add(Math.log(price_hist.get(price_hist.size() - 1) / price_hist.get(i)));
			chart_index_hist.add(chart_index);
			opinion_index_hist.add(opinion_index);
			// chart_wealth_hist.add(chart_hold * price + chart_mon);
			// fund_wealth_hist.add(fund_hold * price + fund_mon);

			System.out.println(String.format(Locale.ENGLISH, "%04d", i + 1) + delim
					+ String.format(Locale.ENGLISH, "%05.2f", price_hist.get(i)) + delim
					+ String.format(Locale.ENGLISH, "%.3f", return_hist.get(i)) + delim
					+ String.format(Locale.ENGLISH, "%.3f", chart_index_hist.get(i)) + delim
					+ String.format(Locale.ENGLISH, "%.3f", opinion_index_hist.get(i)));

		} // End of Outer-Loop (1000 Observations)

		long stop = System.currentTimeMillis();
		long elapsed = stop - start;

		System.out.println("Simulation finished! That took: " + (double) elapsed / 1000 + " seconds.");
		// System.out.println("BREAK");
		// for (Agent a : agentslist) {
		// System.out.println(a.getStateList() + delim + a.getHolding() + delim +
		// a.getCash());
		// }

	}

}

//
// if (a.getState() == State.FUNDAMENTALIST) { // If Trader is a Fundamentalist
// if (rnd > 0.5) { // He meets with equal probability a trader of
// if (r.nextDouble() < f_to_o && Agent.getFcount() > agents_min
// && ((Agent.getCcount()) < (agents * chart_max))) {// Optimistic
// // group
// // and switches
// a.setState(State.OPTIMIST);
// Agent.addOcount();
// Agent.dropFcount();
// } else {
// a.setState(State.FUNDAMENTALIST);
// }
// } else {
// if (r.nextDouble() < f_to_p && Agent.getFcount() > agents_min
// && ((Agent.getCcount()) < (agents * chart_max))) {// Pessimistic
// // group
// // and switches
// a.setState(State.PESSIMIST);
// Agent.addPcount();
// Agent.dropFcount();
// } else {
// a.setState(State.FUNDAMENTALIST);
// }
// }
//
// } else if (a.getState() == State.OPTIMIST) { // If trader is an Optimist
// if (rnd > 0.5) { // He meets with equal probability a trader of
// if (r.nextDouble() < o_to_p && Agent.getOcount() > agents_min) {//
// Pessimistic group
// // and switches
// a.setState(State.PESSIMIST);
// Agent.addPcount();
// Agent.dropOcount();
// } else {
// a.setState(State.OPTIMIST);
// }
// } else {
// if (r.nextDouble() < o_to_f && Agent.getOcount() > agents_min) {//
// Fundamentalistic
// // group and
// // switches
// a.setState(State.FUNDAMENTALIST);
// Agent.addFcount();
// Agent.dropOcount();
// } else {
// a.setState(State.OPTIMIST);
// }
// }
//
// } else if (a.getState() == State.PESSIMIST) { // If trader is Pessimist
// if (rnd > 0.5) { // He meets with equal probability a trader of
// if (r.nextDouble() < p_to_o && Agent.getPcount() > agents_min) {// Optimistic
// group
// // and switches
// a.setState(State.OPTIMIST);
// Agent.addOcount();
// Agent.dropPcount();
// } else {
// a.setState(State.PESSIMIST);
// }
// } else {
// if (r.nextDouble() < p_to_f && Agent.getPcount() > agents_min) {//
// Fundamentalistic
// // group and
// // switches
// a.setState(State.FUNDAMENTALIST);
// Agent.addFcount();
// Agent.dropPcount();
// } else {
// a.setState(State.PESSIMIST);
// }
// }
// }

// p_to_o = v1 * (((double) Agent.getCcount() / agents) * Math.exp(u1)) / steps;
// o_to_p = v1 * (((double) Agent.getCcount() / agents) * Math.exp(-u1)) /
// steps;
//
// f_to_o = v2 * (((double) Agent.getOcount() / agents) * Math.exp(u2)) / steps;
// o_to_f = v2 * (((double) Agent.getFcount() / agents) * Math.exp(-u2)) /
// steps;
//
// f_to_p = v2 * (((double) Agent.getPcount() / agents) * Math.exp(u3)) / steps;
// p_to_f = v2 * (((double) Agent.getFcount() / agents) * Math.exp(-u3)) /
// steps;
