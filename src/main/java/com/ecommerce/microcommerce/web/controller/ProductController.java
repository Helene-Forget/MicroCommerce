package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.Dao.ProductDao;
import com.ecommerce.microcommerce.Model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class ProductController {

    @Autowired//Spring se charge d'en fabriquer une instance
    private ProductDao productDao;

    //cette méthode est remplacée par la suivante améliorée avec un filtre dynamique
    /*@RequestMapping( value= "/Produits" , method= RequestMethod.GET)
    public List<Product> listeProduits() {
        return productDao.findAll();

    }*/

    //Récupérer la liste des produits
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)
    public MappingJacksonValue listeProduits() {
        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }

    // cette méthode est remplacée par la suivante pour renvoyer un json plutot qu'une phrase
    /*@RequestMapping(value = "/Produits/{id}", method = RequestMethod.GET)
    public String afficherUnProduit(@PathVariable int id) {
        return "Vous avez demandé un produit avec l'id  " + id;
    }*/

    //Récupérer un produit par son Id
    @GetMapping(value="/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {

        return productDao.findById(id);
    }

    //ajouter un produit
   /* @PostMapping(value = "/Produits")
    public void ajouterProduit(@RequestBody Product product) {
        productDao.save(product);
    }*/

    //ajouter un produit amélioré avec une  URI de renvoi de réponse permet de définir le code HTTP  à retourner
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@RequestBody Product product) {

        Product productAdded =  productDao.save(product);

        if (productAdded == null)
            return ResponseEntity.noContent().build();
        /*if (productAdded.getPrixAchat() == null)
            return ResponseEntity.noContent().build();*/
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
    //afficher les produits dont le prix est supérieur à un prix limite
    @GetMapping(value = "test/Produits/{prixLimit}")
    public List<Product> testeDeRequetes(@PathVariable int prixLimit) {
        return productDao.findByPrixGreaterThan(prixLimit);
    }
    //afficher les produits dont le nom  contient un mot
    @GetMapping(value = "test/produits/{recherche}")
    public List<Product> testeDeRequetes(@PathVariable String recherche) {
        return productDao.findByNomLike("%"+recherche+"%");
    }
    //supprimer un produit
    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.deleteById(id);
    }
    //modifier un produit
    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }

    //notre propre requete
    @GetMapping(value = "/Produits/{prixLimit}")
    public List<Product>  chercherUnProduitCher(@Param("prixLimit") int prix){
        return productDao.findAllPrixGreaterThan(prix);
    }

    //pour trier les produits par ordre alphabétique
    @GetMapping(value = "test/produits/ordreAlphabétique")
    public List<Product> trierProduitsParOrdreAlphabetique() {
        List<Product> productsSorted = productDao.findByOrderByNomDesc();
        return productsSorted;
    }

    //Pour calculer la marge d'un produit donné par son id( prix de vente - prix d'achat
    @GetMapping(value = "test/produits/marge/{id}")
    public String  calculerMargeProduit(@PathVariable int id) {
        Product product = productDao.findById(id);
        if(product == null){
            throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. ");
        }
        int marge = product.getPrix()-product.getPrixAchat();
        System.out.println(marge);
        return "la marge de "+product.getNom()+" est de "+ marge;
    }

    //Pour calculer la marge de tous les produits
    @GetMapping(value = "/adminProduits")
    public String  calculerMargeProduits() {
        List<Product> products = productDao.findAll();
        String marges ="";
        for(Product p:products) {
            int marge = p.getPrix() - p.getPrixAchat();
            marges += p.toString() + " : marge " + marge +System.lineSeparator() ;
        }
        return marges;
    }
}
