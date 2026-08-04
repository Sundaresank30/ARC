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
