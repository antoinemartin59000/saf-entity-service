package com.antoinemartin59000.saf.entityservice;

import javax.sql.DataSource;

import com.antoinemartin59000.saf.AbstractStrictCloseable;
import com.antoinemartin59000.saf.entitydao.DaoSession;

public class SafServiceSession extends AbstractStrictCloseable {

    public enum ServiceSessionInitiatorType {
        VISITOR,
        PLAYER,
        ADMINISTRATOR,
        PROCESS;
    }

    private final ServiceSessionInitiatorType serviceSessionInitiatorType;
    private final Long id;

    private final DaoSession daoSession;

    public SafServiceSession(DataSource dataSource, ServiceSessionInitiatorType serviceSessionInitiatorType, Long id) {
        this.serviceSessionInitiatorType = serviceSessionInitiatorType;
        this.id = id;

        this.daoSession = new DaoSession(dataSource);
    }

    // package visibility as only EntityService is meant to use it
    DaoSession getDaoSession() {
        return daoSession;
    }

    public boolean isDeityOrCode() {
        return serviceSessionInitiatorType == ServiceSessionInitiatorType.PROCESS || serviceSessionInitiatorType == ServiceSessionInitiatorType.ADMINISTRATOR || this.getTransactionStackSize() > 1;
    }

    public boolean isDeityOrCodeOrPlayer(Long id) {
        return isDeityOrCode() || (this.serviceSessionInitiatorType == ServiceSessionInitiatorType.PLAYER && id.equals(this.id));
    }

    @Override
    protected void onOpenTransaction(boolean enableFullStackTrace) {
        daoSession.openTransaction(enableFullStackTrace);
    }

    @Override
    protected void onCommit() {
        daoSession.commit();
    }

    @Override
    protected void onCloseTransaction() {
        daoSession.closeTransaction();
    }

    @Override
    public void onClosing() {
        daoSession.close();
    }

}
