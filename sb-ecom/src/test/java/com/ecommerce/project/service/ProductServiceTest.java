package com.ecommerce.project.service;


import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {


    @Nested
    @ExtendWith(MockitoExtension.class)
    class ProductServiceImplTest {

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private ProductRepository productRepository;

        @Mock
        private ModelMapper modelMapper;

        @InjectMocks
        private ProductServiceImpl productService;

        private Category category;
        private ProductDTO productDTO;
        private Product product;

        @BeforeEach
        void setUp() {
            category = new Category();
            category.setCategoryId(1L);

            productDTO = new ProductDTO();
            productDTO.setProductName("Dumbbell");
            productDTO.setPrice(1000.0);
            productDTO.setDiscount(10.0);

            product = new Product();
            product.setProductName("Dumbbell");
            product.setPrice(1000.0);
            product.setDiscount(10.0);
        }

        @Test
        void addProduct_ShouldSaveProduct_WhenNotExists() {
            // given
            category.setProducts(List.of()); // no existing products
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(modelMapper.map(productDTO, Product.class)).thenReturn(product);
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

            // when
            ProductDTO savedProductDTO = productService.addProduct(1L, productDTO);

            // then
            assertThat(savedProductDTO).isNotNull();
            assertThat(savedProductDTO.getProductName()).isEqualTo("Dumbbell");
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void addProduct_ShouldThrowException_WhenProductAlreadyExists() {
            // given
            category.setProducts(List.of(product)); // product already exists
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            // when + then
            assertThatThrownBy(() -> productService.addProduct(1L, productDTO))
                    .isInstanceOf(APIException.class)
                    .hasMessage("Product already exist!!");

            verify(productRepository, never()).save(any());
        }

        @Test
        void addProduct_ShouldThrowException_WhenCategoryNotFound() {
            // given
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> productService.addProduct(1L, productDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category");

            verify(productRepository, never()).save(any());
        }
    }
}