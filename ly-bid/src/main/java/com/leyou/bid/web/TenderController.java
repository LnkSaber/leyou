package com.leyou.bid.web;

import com.leyou.bid.pojo.Tender;
import com.leyou.bid.service.TenderService;
import com.leyou.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tender")
public class TenderController {
    @Autowired
    private TenderService tenderService;
    @GetMapping("user/page")
    public ResponseEntity<PageResult<Tender>> queryUserTanderByPage(
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "uid", required = true) Long uid,
            @RequestParam(value = "sortBy" ,required = false) String sortBy,
            @RequestParam(value = "desc" ,defaultValue = "false") Boolean desc,
            @RequestParam(value = "key" , required = false) String key
    ){
        return ResponseEntity.ok(tenderService.queryUserTanderByPage(uid, page, rows, sortBy, desc, key));
    }

    @GetMapping("vender/page")
    public ResponseEntity<PageResult<Tender>> queryVenderByPage(
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy" ,required = false) String sortBy,
            @RequestParam(value = "desc" ,defaultValue = "false") Boolean desc,
            @RequestParam(value = "key" , required = false) String key
    ){
        return ResponseEntity.ok(tenderService.queryVenderByPage(page, rows, sortBy, desc, key));
    }

    @GetMapping("admin/page")
    public ResponseEntity<PageResult<Tender>> queryAdminByPage(
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy" ,required = false) String sortBy,
            @RequestParam(value = "desc" ,defaultValue = "false") Boolean desc,
            @RequestParam(value = "key" , required = false) String key
    ){
        return ResponseEntity.ok(tenderService.queryAdminByPage(page, rows, sortBy, desc, key));
    }

    @PutMapping("admin/tender/state/{tenderid}")
    public ResponseEntity<Void> updateTenderStateByAdmin(@PathVariable("tenderid") Long tenderid){
        tenderService.updateTenderStateByAdmin(tenderid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("user/tender")
    public ResponseEntity<Void> saveTender(@RequestBody Tender tender){
        tenderService.saveTender(tender);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PutMapping("user/state/remove/{tenderid}")
    public ResponseEntity<Void> updateTenderRemoveStateByUser(@PathVariable("tenderid") Long tenderid){
        tenderService.updateTenderRemoveStateByUser(tenderid);
        return ResponseEntity.ok().build();
    }
    @PutMapping("user/state/choose/{tenderid}/{bidid}")
    public ResponseEntity<Void> updateTenderChooseStateByUser(
            @PathVariable("tenderid") Long tenderid, @PathVariable("bidid") Long bidid){
        tenderService.updateTenderRemoveChooseByUser(tenderid, bidid);
        return ResponseEntity.ok().build();
    }

}
