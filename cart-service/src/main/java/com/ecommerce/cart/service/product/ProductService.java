package com.ecommerce.cart.service.product;

import com.ecommerce.cart.dto.CreateProductRequest;
import com.ecommerce.cart.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request);
    List<ProductDto> getProducts();
}
