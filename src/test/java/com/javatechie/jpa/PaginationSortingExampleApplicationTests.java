package com.javatechie.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Random;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import com.javatechie.jpa.entity.Product;
import com.javatechie.jpa.service.ProductService;

@SpringBootTest
class PaginationSortingExampleApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private ProductService productService;

	@Test
	void testSaveProduct() {
		Product product = new Product("TestProduct", 10, 1000);
		Product savedProduct = productService.saveProduct(product);

		assertNotNull(savedProduct);
		assertNotNull(savedProduct.getId());
		assertEquals("TestProduct", savedProduct.getName());
		assertEquals(10, savedProduct.getQuantity());
		assertEquals(1000, savedProduct.getPrice());
	}

	@Test
	void testFindAllProducts() {
		List<Product> products = productService.findAllProducts();
		assertNotNull(products);
		assertTrue(products.size() > 0);
	}

	@Test
	void testFindProductsWithSorting() {
		List<Product> sortedProducts = productService.findProductsWithSorting("name");
		assertNotNull(sortedProducts);
		assertTrue(sortedProducts.size() > 0);
		
		// Create a Collator that mimics MySQL's default sorting
		Collator collator = Collator.getInstance(Locale.ENGLISH);
		collator.setStrength(Collator.SECONDARY); // Case-insensitive, but considers accents
		
		// Extract names and create a sorted copy using the collator
		List<String> names = sortedProducts.stream()
			.map(Product::getName)
			.collect(Collectors.toList());
		List<String> sortedNames = new ArrayList<>(names);
		sortedNames.sort(collator);
		
		assertEquals(sortedNames, names, "Products should be sorted by name in MySQL-like order");
	}

	@Test
	void testFindProductsWithPagination() {
		int offset = 0;
		int pageSize = 10;
		Page<Product> productPage = productService.findProductsWithPagination(offset, pageSize);
		assertNotNull(productPage);
		assertEquals(pageSize, productPage.getSize());
		assertTrue(productPage.getContent().size() <= pageSize);
	}

	@Test
	void testFindProductsWithPaginationAndSorting() {
		int offset = 0;
		int pageSize = 10;
		String field = "price";
		Page<Product> productPage = productService.findProductsWithPaginationAndSorting(offset, pageSize, field);
		assertNotNull(productPage);
		assertEquals(pageSize, productPage.getSize());
		assertTrue(productPage.getContent().size() <= pageSize);
		List<Product> content = productPage.getContent();
		for (int i = 0; i < content.size() - 1; i++) {
			assertTrue(content.get(i).getPrice() <= content.get(i + 1).getPrice());
		}
	}

	@Test
	void testGetProductById() {
		// First, save a product
		Product product = new Product("TestProduct" + new Random().nextInt(), 10, 1000);
		Product savedProduct = productService.saveProduct(product);

		// Then, retrieve it by ID
		Product retrievedProduct = productService.getProductById(savedProduct.getId());

		assertNotNull(retrievedProduct);
		assertEquals(savedProduct.getId(), retrievedProduct.getId());
		assertEquals(savedProduct.getName(), retrievedProduct.getName());
		assertEquals(10, retrievedProduct.getQuantity());
		assertEquals(1000, retrievedProduct.getPrice());

		// Test for non-existent product
		assertNull(productService.getProductById(Integer.MAX_VALUE));
	}

}
