package com.ecommerce.microcommerce.Dao;

import com.ecommerce.microcommerce.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDao extends JpaRepository<Product, Integer> {
    public List<Product> findAll();
    public Product findById(int id);
    public Product save(Product product);

    List<Product> findByPrixGreaterThan(int prixLimit);

    List<Product> findByNomLike(String recherche);

    void deleteById(int id);

    List<Product> findByOrderByNomDesc();

    //notre propre requete
    @Query("SELECT id, nom, prix FROM Product p WHERE p.prix > :prixLimit")
    List<Product>  findAllPrixGreaterThan(@Param("prixLimit") int prix);

}
