package com.antoinemartin59000.saf.entityservice;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.antoinemartin59000.saf.common.StatisticalItem;
import com.antoinemartin59000.saf.entity.SafEntity;
import com.antoinemartin59000.saf.entity.SafEntitySearch;
import com.antoinemartin59000.saf.entitydao.SafEntityDao;
import com.antoinemartin59000.saf.entityservice.serviceexception.SafServiceException;

public abstract class SafEntityService<E extends SafEntity, S extends SafEntitySearch> {

    private final SafEntityDao<E, S> dao;
    private final StatisticalItem statisticalItem;

    protected SafEntityService(SafEntityDao<E, S> dao) {
        this.dao = dao;
        this.statisticalItem = StatisticalItem.getRoot().getChild("EntityService").getChild(this.getClass().getSimpleName());
    }

    protected void addTime(String name, long start) {
        statisticalItem.getChild(name).addTime(start);
    }

    public final Map<Long, E> searchFromStartUpCache(SafServiceSession serviceSession) {
        return dao.fetchAllFromStartUpCache(serviceSession.getDaoSession());
    }

    public final List<E> searchFromStartUpCache(SafServiceSession serviceSession, Predicate<E> predicate) {
        return dao.searchFromStartUpCache(serviceSession.getDaoSession(), predicate);
    }

    public final List<E> searchFromCache(SafServiceSession serviceSession, S search) {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction(false);
            try {
                List<E> result = dao.searchFromCache(search);
                return postSearch(serviceSession, result);
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("searchFromCache(ServiceSession serviceSession, S search)", start);
        }
    }

    public final List<E> searchFromDb(SafServiceSession serviceSession, S search) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction(false);
            try {
                if (!serviceSession.isDeityOrCode()) {
                    throw SafServiceException.errorForbidden("You are not allowed to query the DB.");
                }
                List<E> result = dao.searchFromDb(serviceSession.getDaoSession(), search);
                return postSearch(serviceSession, result);
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("searchFromDb(ServiceSession serviceSession, S search)", start);
        }
    }

    public final E searchFromCache(SafServiceSession serviceSession, Long id) {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction(false);
            try {
                E result = dao.searchFromCache(id);
                if (result == null) {
                    return null;
                }
                return postSearch(serviceSession, List.of(result)).stream().findAny().orElse(null);
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("searchFromCache(ServiceSession serviceSession, Long id)", start);
        }
    }

    public final E searchFromDb(SafServiceSession serviceSession, Long id) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction(false);
            try {
                if (!serviceSession.isDeityOrCode()) {
                    throw SafServiceException.errorForbidden("You are not allowed to query the DB.");
                }
                E result = dao.searchFromDb(serviceSession.getDaoSession(), id);
                if (result == null) {
                    return null;
                }
                return postSearch(serviceSession, List.of(result)).stream().findAny().orElse(null);
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("searchFromDb(ServiceSession serviceSession, Long id)", start);
        }
    }

    protected abstract List<E> postSearch(SafServiceSession serviceSession, List<E> result);

    public final Long insert(SafServiceSession serviceSession, E entity) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction();
            try {
                preInsert(serviceSession, entity);
                Long id = dao.insert(serviceSession.getDaoSession(), entity);
                postInsert(serviceSession, id);
                serviceSession.commit();
                return id;
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("insert(ServiceSession serviceSession, E entity)", start);
        }
    }

    protected abstract void preInsert(SafServiceSession serviceSession, E entity) throws SafServiceException;

    protected abstract void postInsert(SafServiceSession serviceSession, Long insertedId) throws SafServiceException;

    protected final Long straightInsert(SafServiceSession serviceSession, E entity) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction();
            try {
                Long id = dao.insert(serviceSession.getDaoSession(), entity);
                serviceSession.commit();
                return id;
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("insert(ServiceSession serviceSession, E entity)", start);
        }
    }

    public final void update(SafServiceSession serviceSession, E updatedVersion) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction();
            try {
                if (updatedVersion.getId() == null) {
                    throw SafServiceException.error("The updated entity must have an id.");
                }

                E existingVersion = searchFromDb(serviceSession, updatedVersion.getId());
                if (existingVersion == null) {
                    throw SafServiceException.errorNotFound("No record found for id " + updatedVersion.getId());
                }

                preUpdate(serviceSession, existingVersion, updatedVersion);
                dao.update(serviceSession.getDaoSession(), updatedVersion);
                postUpdate(serviceSession, existingVersion, updatedVersion);
                serviceSession.commit();
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("update(ServiceSession serviceSession, E updatedVersion)", start);
        }
    }

    protected abstract void preUpdate(SafServiceSession serviceSession, E existingVersion, E updatedVersion) throws SafServiceException;

    protected abstract void postUpdate(SafServiceSession serviceSession, E existingVersion, E updatedVersion) throws SafServiceException;

    protected final void straightUpdate(SafServiceSession serviceSession, E updatedVersion) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction();
            try {
                if (updatedVersion.getId() == null) {
                    throw SafServiceException.error("The updated entity must have an id.");
                }

                dao.update(serviceSession.getDaoSession(), updatedVersion);
                serviceSession.commit();
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("straightUpdate(ServiceSession serviceSession, E updatedVersion)", start);
        }
    }

    public final void delete(SafServiceSession serviceSession, Long id) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction();
            try {

                E existingVersion = searchFromDb(serviceSession, id);
                if (existingVersion == null) {
                    throw SafServiceException.errorNotFound("No record found for id " + id);
                }

                preDelete(serviceSession, existingVersion, id);
                dao.delete(serviceSession.getDaoSession(), id);
                postDelete(serviceSession, existingVersion, id);
                serviceSession.commit();
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("delete(ServiceSession serviceSession, Long id)", start);
        }
    }

    protected abstract void preDelete(SafServiceSession serviceSession, E existingVersion, Long id) throws SafServiceException;

    protected abstract void postDelete(SafServiceSession serviceSession, E existingVersion, Long id) throws SafServiceException;

    protected final void straightDelete(SafServiceSession serviceSession, Long id) throws SafServiceException {
        long start = System.currentTimeMillis();
        try {
            serviceSession.openTransaction();
            try {

                dao.delete(serviceSession.getDaoSession(), id);
                serviceSession.commit();
            } finally {
                serviceSession.closeTransaction();
            }
        } finally {
            addTime("straightDelete(ServiceSession serviceSession, Long id)", start);
        }
    }
}
