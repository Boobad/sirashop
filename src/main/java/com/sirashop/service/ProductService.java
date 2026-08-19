package com.sirashop.service;

import com.sirashop.dto.ProductDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.Product;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    public ProductDto createProduct(ProductDto dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBarcode(dto.getBarcode());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setCompany(company);

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public List<ProductDto> getProductsByCompany(Long companyId) {
        return productRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBarcode(product.getBarcode());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setCompanyId(product.getCompany().getId());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
}
