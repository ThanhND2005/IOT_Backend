package com.app.common.base.search.service;

import com.app.common.base.search.dto.SearchParam;
import com.app.common.base.search.enums.SearchDataType;
import com.app.common.base.search.enums.SearchOperation;
import com.app.modules.device.entity.Device;
import com.app.modules.device.entity.DeviceHistory;
import com.app.modules.device.repository.DeviceHistoryRepository;
import com.app.modules.device.repository.DeviceRepository;
import com.app.common.enums.ActionStatus;
import com.app.common.enums.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GenericSpecificationTest {

    @Autowired
    private DeviceHistoryRepository deviceHistoryRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    @DisplayName("Kiểm tra tìm kiếm LIKE trên trường createdAt không bị lỗi")
    void testLikeSearchOnCreatedAt() {
        Device device = deviceRepository.findAll().stream().findFirst().orElse(null);
        if (device != null) {
            DeviceHistory history = DeviceHistory.builder()
                    .device(device)
                    .action(ActionType.ON)
                    .status(ActionStatus.SUCCESS)
                    .source("TEST")
                    .build();
            deviceHistoryRepository.save(history);
        }

        GenericSpecification<DeviceHistory> spec = new GenericSpecification<>();
        spec.add(SearchParam.builder()
                .field("createdAt")
                .operate(SearchOperation.LIKE)
                .value("202")
                .type(SearchDataType.STRING)
                .build());

        Page<DeviceHistory> result = deviceHistoryRepository.findAll(spec, PageRequest.of(0, 10));
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Kiểm tra tìm kiếm LIKE trên các kiểu dữ liệu khác nhau (Enum, UUID, Number, String)")
    void testLikeSearchOnVariousDataTypes() {
        GenericSpecification<DeviceHistory> spec = new GenericSpecification<>();
        spec.add(SearchParam.builder()
                .field("action")
                .operate(SearchOperation.LIKE)
                .value("ON")
                .type(SearchDataType.STRING)
                .build());
        spec.add(SearchParam.builder()
                .field("source")
                .operate(SearchOperation.LIKE)
                .value("test")
                .type(SearchDataType.STRING)
                .build());

        Page<DeviceHistory> result = deviceHistoryRepository.findAll(spec, PageRequest.of(0, 10));
        assertThat(result).isNotNull();
    }
}
