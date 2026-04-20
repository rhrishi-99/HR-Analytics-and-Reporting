package com.hranalytics.domain;

/**
 * Domain class representing a department in the HRMS.
 * Read-only data sourced from the HRMS core.
 */
public class Department {

    private final String deptId;
    private final String deptName;
    private final String manager;
    private int headcount;

    public Department(String deptId, String deptName, String manager, int headcount) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.manager = manager;
        this.headcount = headcount;
    }

    public String getDeptId()   { return deptId; }
    public String getDeptName() { return deptName; }
    public String getManager()  { return manager; }
    public int getHeadcount()   { return headcount; }

    public void setHeadcount(int headcount) { this.headcount = headcount; }

    @Override
    public String toString() {
        return String.format("Department[%s, %s, headcount=%d]", deptId, deptName, headcount);
    }
}
