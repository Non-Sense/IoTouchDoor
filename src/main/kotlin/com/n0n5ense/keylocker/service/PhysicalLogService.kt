package com.n0n5ense.keylocker.service

import com.n0n5ense.keylocker.mapper.PhysicalLogMapper
import com.n0n5ense.keylocker.model.ColumnLimit
import com.n0n5ense.keylocker.model.PhysicalLogModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PhysicalLogService @Autowired constructor(private val dao: PhysicalLogMapper) {

    fun insert(log: PhysicalLogModel):Boolean{
        return dao.insert(log) > 0
    }

    fun selectAll(limit: ColumnLimit):List<PhysicalLogModel>{
        return dao.selectAll(limit)
    }
}