package com.n0n5ense.keylocker

import com.n0n5ense.keylocker.door.DoorController
import com.n0n5ense.keylocker.felica.FelicaLogger
import com.n0n5ense.keylocker.model.ColumnLimit
import com.n0n5ense.keylocker.model.CardTouchLogViewModel
import com.n0n5ense.keylocker.model.PhysicalLogModel
import com.n0n5ense.keylocker.model.UserModel
import com.n0n5ense.keylocker.service.CardTouchLogService
import com.n0n5ense.keylocker.service.PhysicalLogService
import org.springframework.beans.factory.annotation.Autowired
import com.n0n5ense.keylocker.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.web.bind.annotation.*
import kotlin.math.min

@RestController
@RequestMapping("/api")
class RestApiController @Autowired constructor(
    private val userService: UserService,
    private val cardTouchLogService: CardTouchLogService,
    private val physicalLogService: PhysicalLogService,
    private val felicaLogger: FelicaLogger,
    private val doorController: DoorController) {

    init {
        felicaLogger.init()
    }

    @RequestMapping("/door/unlock")
    fun unlockDoor(): ResponseEntity<String>{
        doorController.unlock()
        return ResponseEntity.ok(null)
    }

    @RequestMapping("/door/lock")
    fun lockDoor(): ResponseEntity<String>{
        doorController.lock()
        return ResponseEntity.ok(null)
    }

    @GetMapping("/door/status")
    fun door(): ResponseEntity<Map<String,Boolean>>{
        val map:HashMap<String,Boolean> = HashMap()
        val status = doorController.getStatus()
        map["active"] = status != null
        map["closed"] = status?.isClose == true
        map["locked"] = status?.isLock == true
        return ResponseEntity.ok(map)
    }


    @RequestMapping("/start")
    fun start(): ResponseEntity<String>{
        return felicaLogger.init()
    }

    @RequestMapping("/close")
    fun close(): ResponseEntity<Unit>{
        felicaLogger.close()
        return ResponseEntity(null,HttpStatus.NO_CONTENT)
    }

    @RequestMapping("/status")
    fun status(): String{
        return felicaLogger.status()
    }

    @PostMapping("/user")
    fun addUser(@RequestBody user: UserModel): ResponseEntity<UserModel>{
        if(user.cardId.length != 16)
            return ResponseEntity(null,HttpStatus.BAD_REQUEST)
        try {
            userService.insert(user)
        } catch (e: UncategorizedSQLException){
            return ResponseEntity(null, HttpStatus.CONFLICT)
        }
        userService.select(user.cardId).let {
            if(it!=null)
                return ResponseEntity.ok(it)
        }
        return ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PutMapping("/user")
    fun editUser(@RequestBody user: UserModel):ResponseEntity<UserModel>{
        userService.update(user).let {
            if(!it)
                return ResponseEntity(null,HttpStatus.BAD_REQUEST)
            return ResponseEntity.ok(user)
        }
    }

    @DeleteMapping("/user")
    fun deleteUser(@RequestBody user: UserModel):ResponseEntity<Unit>{
        userService.delete(user).let {
            if(!it)
                return ResponseEntity(null,HttpStatus.NOT_FOUND)
            return ResponseEntity(null,HttpStatus.NO_CONTENT)
        }
    }

    @GetMapping("/user")
    fun findUser(@RequestParam(name="q",required = true)id:String): ResponseEntity<UserModel>{
        if(id.length != 16)
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        val user = userService.select(id)
        if(user==null)
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/users")
    fun getUsers(): ResponseEntity<List<UserModel>>{
        return ResponseEntity.ok(userService.selectAll())
    }

    @GetMapping("/logs")
    fun addTouchLog(@RequestParam(name="l",required=false) limit: Int?, @RequestParam(name="o",required=false) offset:Int?): ResponseEntity<List<CardTouchLogViewModel>>{
        val l = min(limit?:30,100)
        return ResponseEntity.ok(cardTouchLogService.selectAll(ColumnLimit(l,offset?:0)))
    }

    @GetMapping("/physicallogs")
    fun getPhysicalLog(@RequestParam(name="l",required=false) limit: Int?, @RequestParam(name="o",required=false) offset:Int?): ResponseEntity<List<PhysicalLogModel>>{
        val l = min(limit?:30,100)
        return ResponseEntity.ok(physicalLogService.selectAll(ColumnLimit(l,offset?:0)))
    }
}