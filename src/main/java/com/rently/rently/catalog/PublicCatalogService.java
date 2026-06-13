package com.rently.rently.catalog;

import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.catalog.features.Feature;
import com.rently.rently.catalog.models.Model;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;

/**
 * Read-only service that always accesses the public schema, bypassing the
 * multi-tenancy CurrentTenantIdentifierResolver. Used by tenant-scoped
 * services (e.g., VehicleService) that need to validate cross-schema
 * references to Brand, Model, and Feature.
 */
@Service
public class PublicCatalogService {

    private final EntityManagerFactory entityManagerFactory;

    public PublicCatalogService(@Qualifier("publicEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public Brand getBrand(String brandId) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Brand brand = em.find(Brand.class, brandId);
            if (brand == null) throw new EntityNotFoundException("Brand not found: " + brandId);
            return brand;
        } finally {
            em.close();
        }
    }

    public boolean brandExists(String brandId) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.find(Brand.class, brandId) != null;
        } finally {
            em.close();
        }
    }

    public Model getModel(String modelId) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Model model = em.find(Model.class, modelId);
            if (model == null) throw new EntityNotFoundException("Model not found: " + modelId);
            return model;
        } finally {
            em.close();
        }
    }

    public Model getModelWithBrand(String modelId) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            List<Model> result = em.createQuery(
                    "SELECT m FROM Model m JOIN FETCH m.brand WHERE m.id = :id", Model.class)
                    .setParameter("id", modelId)
                    .getResultList();
            if (result.isEmpty()) throw new EntityNotFoundException("Model not found: " + modelId);
            return result.get(0);
        } finally {
            em.close();
        }
    }

    public boolean modelExists(String modelId) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.find(Model.class, modelId) != null;
        } finally {
            em.close();
        }
    }

    public List<Feature> getFeatures(Set<String> featureIds) {
        if (featureIds.isEmpty()) return List.of();
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery("SELECT f FROM Feature f WHERE f.id IN :ids", Feature.class)
                    .setParameter("ids", featureIds)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean allFeaturesExist(Set<String> featureIds) {
        if (featureIds.isEmpty()) return true;
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(f) FROM Feature f WHERE f.id IN :ids", Long.class)
                    .setParameter("ids", featureIds)
                    .getSingleResult();
            return count == featureIds.size();
        } finally {
            em.close();
        }
    }
}
