package com.arc.datapreparation.service;

import com.arc.datapreparation.dto.CreateProductionBatchRequest;
import com.arc.datapreparation.dto.ProductionBatchItemDto;
import com.arc.datapreparation.dto.ProductionBatchResponse;
import com.arc.datapreparation.entity.ProductionBatch;
import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataPreparationService {

    private final ProductionBatchRepository batchRepository;
    private final ProductionBatchItemRepository itemRepository;

    @Transactional
    public ProductionBatchResponse createProductionBatch(CreateProductionBatchRequest request) {
        String batchId = request.getBatchId().trim();

        if (batchRepository.existsByBatchId(batchId)) {
            throw new IllegalArgumentException("Production batch with ID '" + batchId + "' already exists.");
        }

        int totalCount = Math.max(request.getPartNoCount(), request.getSerialNoCount());

        ProductionBatch batch = ProductionBatch.builder()
                .batchId(batchId)
                .partNoSeries(request.getPartNoSeries().trim())
                .partNoCount(request.getPartNoCount())
                .serialNoSeries(request.getSerialNoSeries().trim())
                .serialNoCount(request.getSerialNoCount())
                .totalItems(totalCount)
                .build();

        List<ProductionBatchItem> items = new ArrayList<>();
        for (int i = 1; i <= totalCount; i++) {
            String partNumber = generatePartNumber(request.getPartNoSeries().trim(), i);
            String serialNumber = generateSerialNumber(request.getSerialNoSeries().trim(), i);

            ProductionBatchItem item = ProductionBatchItem.builder()
                    .itemIndex(i)
                    .partNumber(partNumber)
                    .serialNumber(serialNumber)
                    .status("PREPARED")
                    .build();

            batch.addItem(item);
            items.add(item);
        }

        ProductionBatch savedBatch = batchRepository.save(batch);

        return mapToResponse(savedBatch, true);
    }

    @Transactional(readOnly = true)
    public List<ProductionBatchResponse> getAllBatches() {
        return batchRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(batch -> mapToResponse(batch, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductionBatchResponse getBatchByBatchId(String batchId) {
        ProductionBatch batch = batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
        return mapToResponse(batch, true);
    }

    @Transactional(readOnly = true)
    public List<ProductionBatchItemDto> getBatchItems(String batchId) {
        return itemRepository.findByProductionBatchBatchIdOrderByItemIndexAsc(batchId)
                .stream()
                .map(item -> ProductionBatchItemDto.builder()
                        .id(item.getId())
                        .itemIndex(item.getItemIndex())
                        .partNumber(item.getPartNumber())
                        .serialNumber(item.getSerialNumber())
                        .status(item.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public String generatePartNumber(String series, int index) {
        String seq = String.format("%02d", index);
        if (series == null || series.trim().isEmpty()) {
            return "PN" + seq;
        }
        String clean = series.trim();
        boolean hasTrailingLetter = Character.isLetter(clean.charAt(clean.length() - 1));

        if (hasTrailingLetter) {
            char suffix = clean.charAt(clean.length() - 1);
            String base = clean.substring(0, clean.length() - 1);
            if (base.matches(".*\\d{2}$")) {
                base = base.substring(0, base.length() - 2);
            }
            return base + seq + suffix;
        } else {
            if (clean.matches(".*\\d{2}$")) {
                return clean.substring(0, clean.length() - 2) + seq;
            }
            return clean + seq;
        }
    }

    public String generateSerialNumber(String series, int index) {
        String seq = String.format("%02d", index);
        if (series == null || series.trim().isEmpty()) {
            return "P" + seq;
        }
        String clean = series.trim();
        if (clean.matches(".*\\d{2}$")) {
            return clean.substring(0, clean.length() - 2) + seq;
        }
        return clean + seq;
    }

    private ProductionBatchResponse mapToResponse(ProductionBatch batch, boolean includeItems) {
        List<ProductionBatchItemDto> itemDtos = null;
        if (includeItems && batch.getItems() != null) {
            itemDtos = batch.getItems().stream()
                    .map(item -> ProductionBatchItemDto.builder()
                            .id(item.getId())
                            .itemIndex(item.getItemIndex())
                            .partNumber(item.getPartNumber())
                            .serialNumber(item.getSerialNumber())
                            .status(item.getStatus())
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductionBatchResponse.builder()
                .id(batch.getId())
                .batchId(batch.getBatchId())
                .partNoSeries(batch.getPartNoSeries())
                .partNoCount(batch.getPartNoCount())
                .serialNoSeries(batch.getSerialNoSeries())
                .serialNoCount(batch.getSerialNoCount())
                .totalItems(batch.getTotalItems())
                .createdAt(batch.getCreatedAt())
                .items(itemDtos)
                .build();
    }
}
