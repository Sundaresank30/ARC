package com.arc.machine.service;

import com.arc.machine.dto.MachineRecordDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class MachineService {

    private final ConcurrentMap<Long, MachineRecordDto> records = new ConcurrentHashMap<>();

    public MachineService() {
        initInitialRecords();
    }

    public synchronized void initInitialRecords() {
        records.clear();
        records.put(1L, new MachineRecordDto(1L, "SN-1001", "PN-A89", "waiting"));
        records.put(2L, new MachineRecordDto(2L, "SN-1002", "PN-A90", "waiting"));
        records.put(3L, new MachineRecordDto(3L, "SN-1003", "PN-A91", "waiting"));
        records.put(4L, new MachineRecordDto(4L, "SN-1004", "PN-A92", "waiting"));
        records.put(5L, new MachineRecordDto(5L, "SN-1005", "PN-A93", "waiting"));
    }

    public List<MachineRecordDto> getAllRecords() {
        List<MachineRecordDto> list = new ArrayList<>(records.values());
        list.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        return Collections.unmodifiableList(list);
    }

    public MachineRecordDto updateRecordStatus(Long id, String newStatus) {
        MachineRecordDto record = records.get(id);
        if (record != null) {
            record.setStatus(newStatus);
            return record;
        }
        return null;
    }

    public List<MachineRecordDto> resetRecords() {
        initInitialRecords();
        return getAllRecords();
    }
}
