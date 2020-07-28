package eu.fbk.das.challenge.gui.rs;

import eu.fbk.das.model.ChallengeExpandedDTO;
import eu.fbk.das.rs.challenges.generation.RecommendationSystem;
import eu.fbk.das.utils.PolynomialRegression;
import it.smartcommunitylab.model.PlayerStateDTO;
import it.smartcommunitylab.model.ext.ChallengeConcept;
import it.smartcommunitylab.model.ext.GameConcept;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static eu.fbk.das.rs.challenges.generation.RecommendationSystem.fixMode;
import static eu.fbk.das.utils.Utils.*;

public class RecommenderSystemAnalyzer {

    private final RecommendationSystem rs;

    private String mode;

    private DateTime start;

    private int week;

    private double[] weekValues;

    private XYSeriesCollection dataset;

    double[][] dataTest = new double[][] {
            { 0.1, 13.0, 21.0, 18.0, 30.0, 25.0, 28.0, 29.23},
            { 0.1, 0.1, 72.06, 95.47, 0.1, 100.420, 112.748, 165.76},
            { 0.1, 9.30, 16.5, 15.786, 28.06, 58.23, 32, 37.179},
            { 0.1, 173.0, 35.0, 192.0, 127.0, 63.0, 190.0, 126.40},
            { 0.1, 0.1, 0.1, 28.00, 0.1, 12.95, 25.25, 22.20},
            { 0.1, 13.36, 18.82, 53.42, 13.76, 20.14, 8.73, 27.49},
            { 0.1, 22.57, 39.18, 21.83, 11.84, 0.1, 0.1, 19.86},
            { 0.1, 8.0, 24.0, 28.0, 22.0, 18.0, 20.0, 22.91},
            { 0.1, 156.0, 453.0, 344.0, 844.0, 727.0, 555.0, 545.86},
    };

    private HashMap<String, Double> pred_error;


    public RecommenderSystemAnalyzer(RecommendationSystem rs) {
        this.rs = rs;
    }

    public XYDataset createPredictDataset(ChallengeExpandedDTO cha, PlayerStateDTO player, int select) {
        prepare(cha, player);

        addWeeklyDataset();

        if (select == 0)
            predictWMA(4, 1.3, -1);
        else if (select == 1)
            predictPF();
        else if (select == 2)
            predictLinear(-1);

        return dataset;
    }

    // Weighted moving average
    public void predictWMA(int v, double booster, int w) {

        // Last 3 values?
        double den = 0;
        double num = 0;
        for (int ix = 0; ix < v; ix++) {
            // weight * value
            int it = week - (ix +1);
            den += (v-ix) * weekValues[it];
            num += (v-ix);
        }

        double pv = den * booster / num;

        // check last value

        String s = f("wma-%d-%.2f", v, booster);


        XYSeries series = new XYSeries(s);
        series.add(week, pv);
        dataset.addSeries(series);

        addError(s, pv, w);
    }

    private void addError(String s, double pv, int w) {
        if (w < 0)
            return;
        if (!pred_error.containsKey(s))
            pred_error.put(s, 0.0);

        double tr = dataTest[w][week];

        // MAPE
        pred_error.put(s, pred_error.get(s) + Math.abs(pv - tr)/ tr * 1.0);
    }

    // Polynomial fit
    public void predictPF() {

        // Last 3 values?
        int v = 3;
        double[] x = new double[v];
        double[] y = new double[v];
        for (int ix = 0 ; ix < v; ix++) {
            // weight * value
            int it = week - (v -ix);
            x[ix] = weekValues[it];
            y[ix] = it;
        }

        PolynomialRegression regression = new PolynomialRegression(x, y, 3);
        double pv = regression.predict(week);

        XYSeries series = new XYSeries("polinomial_fit");
        series.add(week, pv);
        dataset.addSeries(series);
    }

    public void predictLinear(int w) {

        // Last 3 values?
        int v = 4;
        double[][] d = new double[v][];
        for (int ix = 0 ; ix < v; ix++) {
            // weight * value
            int it = week - (v -ix);
            d[ix] = new double[2];
            d[ix][1] = weekValues[it];
            d[ix][0] = it;
        }

        // creating regression object, passing true to have intercept term
        SimpleRegression simpleRegression = new SimpleRegression(true);
        simpleRegression.addData(d);

        for (double[] a: d)
            System.out.println(Arrays.toString(a));

        // querying for model parameters
        System.out.println("slope = " + simpleRegression.getSlope());
        System.out.println("intercept = " + simpleRegression.getIntercept());

        double pv = simpleRegression.getIntercept() + simpleRegression.getSlope() * week;

        if (simpleRegression.getSlope() < 0)
            pv *= 1.1;
        else
            pv *= 0.9;

        if (pv < 0)
            pv = 1;

        XYSeries series = new XYSeries("linear fit");
        series.add(week, pv);
        dataset.addSeries(series);

        addError("lf" + v, pv, w);
    }



    public XYDataset createWeeklyDataset(ChallengeExpandedDTO cha, PlayerStateDTO player) {

        prepare(cha, player);

        addWeeklyDataset();

        XYSeries series = new XYSeries("challenge");
        double co = (double) (cha.getData("target"));
        series.add(week, co);
        dataset.addSeries(series);

        XYSeries success_series = new XYSeries("success");
        XYSeries failed_series = new XYSeries("failed");
        for (GameConcept gc : player.getState().get("ChallengeConcept")) {
                ChallengeConcept chal = (ChallengeConcept) gc;

            Map<String, Object> res = chal.getFields();
            if (!res.containsKey("counterName"))
                continue;
            String cm = res.get("counterName").toString().toLowerCase().replace("_", " ");
            if (fixMode(cm).equals(fixMode(mode))) {
                int wk = rs.getChallengeWeek(new DateTime(chal.getStart()));
                double cnt = Double.valueOf(res.get("target").toString());
                if (chal.isCompleted()) {
                    success_series.add(wk, cnt);
                } else {
                    failed_series.add(wk, cnt);
                }
            }


        }

        dataset.addSeries(success_series);
        dataset.addSeries(failed_series);

        return dataset;
    }

    private void prepare(ChallengeExpandedDTO cha, PlayerStateDTO player) {
        mode = (String) cha.getData("counterName");
        start = new DateTime(cha.getStart());
        week = rs.getChallengeWeek(start);

        getWeeklyData(player);

        dataset = new XYSeriesCollection();
    }

    private XYSeriesCollection addWeeklyDataset() {

        XYSeries w_series = new XYSeries("weekly");

        for (int ix = week; ix >= 0; ix--) {
            w_series.add(ix, weekValues[ix]);
        }

        dataset.addSeries(w_series);
        return dataset;
    }

    private void getWeeklyData(PlayerStateDTO player) {
        weekValues = new double[week +1];

        double co;
        DateTime aux = start.minusDays(7);
        for (int ix = week; ix >= 0; ix--) {
            co = rs.getWeeklyContentMode(player, mode, aux);
            weekValues[ix] = Math.max(co, 0.1);
            aux = aux.minusDays(7);
        }

        p(Arrays.toString(weekValues));
    }

    public XYDataset createDailyDataset(ChallengeExpandedDTO cha, PlayerStateDTO player) {

        String mode = (String) cha.getData("counterName");

        DateTime start = new DateTime(cha.getStart());
        int day = rs.getChallengeDay(start);
        DateTime aux = new DateTime();
        XYSeries series = new XYSeries("daily");
        for (int ix = day; ix >= 0 && ix >= day -7; ix--) {
            Double co = rs.getDailyContentMode(player, mode, aux);
            series.add(ix, co);
            aux = aux.minusDays(1);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;

    }


    public XYDataset createCheckPrediction(int ix) {

        mode = null;
        start = new DateTime();
        week = 7;

        dataset = new XYSeriesCollection();

        weekValues = dataTest[ix];
        addWeeklyDataset();
        predictLinear(ix);
        for (int i: new int[] {3, 4, 5})
            for (double boo: new double[] {1.25, 1.3, 1.35})
                    predictWMA(i, boo, ix);


        return dataset;
    }

    public void initError() {
        pred_error = new HashMap<String, Double>();
    }

    public void outputError() {
        for (String meth: pred_error.keySet()) {
            pf("Error for method %s: %.2f \n", meth, pred_error.get(meth) / dataTest.length * 100.0);
        }
    }
}
