package io.cloudslang.lang.tools.build.tester.parallel.collect;


import io.cloudslang.lang.tools.build.tester.IRunTestResults;
import io.cloudslang.lang.tools.build.tester.ISlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.PassedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SkippedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThreadSafeRunTestResults implements IRunTestResults, ISlangTestCaseEventListener {

    private ConcurrentMap<String, TestRun> passedTests;
    private ConcurrentMap<String, TestRun> failedTests;
    private ConcurrentMap<String, TestRun> skippedTests;
    private TreeSet<String> coveredExecutables;
    private TreeSet<String> uncoveredExecutables;

    private final Object lockPassedTests;
    private final Object lockFailedTests;
    private final Object lockSkippedTests;
    private final Object lockCoveredExecutables;
    private final Object lockUncoveredExecutables;

    public ThreadSafeRunTestResults() {
        this.passedTests = new ConcurrentHashMap<>();
        this.failedTests = new ConcurrentHashMap<>();
        this.skippedTests = new ConcurrentHashMap<>();
        coveredExecutables = new TreeSet<>();
        uncoveredExecutables = new TreeSet<>();

        lockPassedTests = new Object();
        lockFailedTests = new Object();
        lockSkippedTests = new Object();
        lockCoveredExecutables = new Object();
        lockUncoveredExecutables = new Object();
    }

    @Override
    public Map<String, TestRun> getPassedTests() {
        synchronized (lockPassedTests) {
            return new HashMap<>(passedTests);
        }
    }

    @Override
    public Map<String, TestRun> getFailedTests() {
        synchronized (lockFailedTests) {
            return new HashMap<>(failedTests);
        }
    }

    @Override
    public Map<String, TestRun> getSkippedTests() {
        synchronized (lockSkippedTests) {
            return new HashMap<>(skippedTests);
        }
    }

    @Override
    public Set<String> getCoveredExecutables() {
        synchronized (lockCoveredExecutables) {
            return new TreeSet<>(coveredExecutables);
        }
    }

    @Override
    public Set<String> getUncoveredExecutables() {
        synchronized (lockUncoveredExecutables) {
            return new TreeSet<>(uncoveredExecutables);
        }
    }

    @Override
    public void addPassedTest(String testCaseName, TestRun testRun) {
        passedTests.put(testCaseName, testRun);
    }

    @Override
    public void addFailedTest(String testCaseName, TestRun testRun) {
        failedTests.put(testCaseName, testRun);
    }

    @Override
    public void addSkippedTest(String testCaseName, TestRun testRun) {
        skippedTests.put(testCaseName, testRun);
    }

    @Override
    public void addCoveredExecutables(Set<String> coveredExecutables) {
        synchronized (lockCoveredExecutables) {
            this.coveredExecutables.addAll(coveredExecutables);
        }
    }

    @Override
    public void addUncoveredExecutables(Set<String> uncoveredExecutables) {
        synchronized (lockUncoveredExecutables) {
            this.uncoveredExecutables.addAll(uncoveredExecutables);
        }
    }

    @Override
    public void onEvent(SlangTestCaseEvent event) {
        SlangTestCase slangTestCase = event.getSlangTestCase();
        if (event instanceof FailedSlangTestCaseEvent) {
            addFailedTest(slangTestCase.getName(), new TestRun(slangTestCase, ((FailedSlangTestCaseEvent) event).getFailureReason()));
        } else if (event instanceof PassedSlangTestCaseEvent) {
            addPassedTest(slangTestCase.getName(), new TestRun(slangTestCase, null));
        } else if (event instanceof SkippedSlangTestCaseEvent) {
            addSkippedTest(slangTestCase.getName(), new TestRun(slangTestCase, "Skipping test: " + slangTestCase.getName() + " because it is not in active test suites"));
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ThreadSafeRunTestResults rhs = (ThreadSafeRunTestResults) obj;
        return new EqualsBuilder()
                .append(this.passedTests, rhs.passedTests)
                .append(this.failedTests, rhs.failedTests)
                .append(this.skippedTests, rhs.skippedTests)
                .append(this.coveredExecutables, rhs.coveredExecutables)
                .append(this.uncoveredExecutables, rhs.uncoveredExecutables)
                .append(this.lockPassedTests, rhs.lockPassedTests)
                .append(this.lockFailedTests, rhs.lockFailedTests)
                .append(this.lockSkippedTests, rhs.lockSkippedTests)
                .append(this.lockCoveredExecutables, rhs.lockCoveredExecutables)
                .append(this.lockUncoveredExecutables, rhs.lockUncoveredExecutables)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(passedTests)
                .append(failedTests)
                .append(skippedTests)
                .append(coveredExecutables)
                .append(uncoveredExecutables)
                .append(lockPassedTests)
                .append(lockFailedTests)
                .append(lockSkippedTests)
                .append(lockCoveredExecutables)
                .append(lockUncoveredExecutables)
                .toHashCode();
    }

}
