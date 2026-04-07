package com.ecommerce.cart.service.product;

import com.ecommerce.cart.dto.CreateProductRequest;
import com.ecommerce.cart.dto.ProductDto;
import com.ecommerce.cart.entity.Product;
import com.ecommerce.cart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductDto createProduct(CreateProductRequest request) {

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .image(request.getImage())
                .build();

        Product saved = productRepository.save(product);
        return ProductDto.from(saved);
    }

    @Override
    public List<ProductDto> getProducts(){
        return this.productRepository.findAll()
                .stream()
                .map(ProductDto::from)
                .toList();
    }
}