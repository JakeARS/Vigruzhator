package org.example.Vigruzhator;

import java.util.Objects;

public class Grant {
    String granteeName;
    boolean selectGrant;
    boolean insertGrant;
    boolean updateGrant;
    boolean deleteGrant;
    boolean truncateGrant;
    boolean referencesGrant;
    boolean triggerGrant;
    private static final String grantTenplate = "\tif exists (\n" +
            "\tselect * from pg_catalog.pg_roles where rolname = '%3$s') then\n" +
            "\tGRANT %1$s ON TABLE %2$s to %3$s;\n" +
            "\tend if;";

    Grant(String granteeName, boolean selectGrant, boolean insertGrant, boolean updateGrant, boolean deleteGrant, boolean truncateGrant, boolean referencesGrant, boolean triggerGrant) {
        this.granteeName = granteeName;
        this.selectGrant = selectGrant;
        this.insertGrant = insertGrant;
        this.updateGrant = updateGrant;
        this.deleteGrant = deleteGrant;
        this.truncateGrant = truncateGrant;
        this.referencesGrant = referencesGrant;
        this.triggerGrant = triggerGrant;
    }

    public String getGranteeName() {
        return granteeName;
    }

    public void setGranteeName(String granteeName) {
        this.granteeName = granteeName;
    }

    public boolean isSelectGrant() {
        return selectGrant;
    }

    public void setSelectGrant(boolean selectGrant) {
        this.selectGrant = selectGrant;
    }

    public boolean isInsertGrant() {
        return insertGrant;
    }

    public void setInsertGrant(boolean insertGrant) {
        this.insertGrant = insertGrant;
    }

    public boolean isUpdateGrant() {
        return updateGrant;
    }

    public void setUpdateGrant(boolean updateGrant) {
        this.updateGrant = updateGrant;
    }

    public boolean isDeleteGrant() {
        return deleteGrant;
    }

    public void setDeleteGrant(boolean deleteGrant) {
        this.deleteGrant = deleteGrant;
    }

    public void setTruncateGrant(boolean truncateGrant) { this.truncateGrant = truncateGrant; }

    public boolean isTruncateGrant() {
        return truncateGrant;
    }

    private String getGrants() {
        String grantInfo = new String();

        grantInfo = grantInfo + (selectGrant && !insertGrant && !updateGrant && !deleteGrant && !truncateGrant && !referencesGrant && !triggerGrant ?
                "select" : (selectGrant ? "select, " : ""));
        grantInfo = grantInfo + (insertGrant && !updateGrant && !deleteGrant && !truncateGrant && !referencesGrant && !triggerGrant ?
                "insert" : (insertGrant ? "insert, " : ""));
        grantInfo = grantInfo + (updateGrant && !deleteGrant && !truncateGrant && !referencesGrant && !triggerGrant ?
                "update" : (updateGrant ? "update, " : ""));
        grantInfo = grantInfo + (deleteGrant && !truncateGrant && !referencesGrant && !triggerGrant ?
                "delete" : (deleteGrant ? "delete, " : ""));
        grantInfo = grantInfo +(truncateGrant && !referencesGrant && !triggerGrant ?
                "truncate" : (truncateGrant ? "truncate, " : ""));
        grantInfo = grantInfo +(referencesGrant && !triggerGrant ?
                "references" : (referencesGrant ? "references, " : ""));
        grantInfo = grantInfo +(triggerGrant ? "trigger" : "");

        return grantInfo;
    }

    public String getGrantString(String tableName) {
        String grantString;
        String grantInfo = getGrants();

        grantString =String.format(grantTenplate, grantInfo, tableName, getGranteeName());

        return grantString;
    }

    @Override
    public String toString() {
        String grantInfo = granteeName + "(";
        grantInfo = grantInfo + getGrants();
        grantInfo = grantInfo + ")";
        return grantInfo;
    }

    //переобпределяем equals, чтобы использование метода Collection.contains сравнивало объекты, как мне надо
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grant grant = (Grant) o;
        return selectGrant == grant.selectGrant &&
                insertGrant == grant.insertGrant &&
                updateGrant == grant.updateGrant &&
                deleteGrant == grant.deleteGrant &&
                truncateGrant == grant.truncateGrant &&
                referencesGrant == grant.referencesGrant &&
                triggerGrant == grant.triggerGrant &&
                Objects.equals(granteeName.toUpperCase(), grant.granteeName.toUpperCase());
    }
}
