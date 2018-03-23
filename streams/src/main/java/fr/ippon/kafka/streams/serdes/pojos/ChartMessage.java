package fr.ippon.kafka.streams.serdes.pojos;

import java.util.List;

public class ChartMessage {

    private List<Chart> charts;

    public ChartMessage() {

    }

    public ChartMessage(List<Chart> charts) {
        this.charts = charts;
    }

    public List<Chart> getCharts() {
        return charts;
    }

    public void setCharts(List<Chart> charts) {
        this.charts = charts;
    }
}
