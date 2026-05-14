package com.profit.dashboard;

import java.util.List;

public record DashboardSummary(String title, List<DashboardMetric> metrics, List<String> nextSteps) {
}
