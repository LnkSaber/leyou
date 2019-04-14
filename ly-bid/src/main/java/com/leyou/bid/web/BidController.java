package com.leyou.bid.web;

import com.leyou.bid.pojo.Bid;
import com.leyou.bid.pojo.Tender;
import com.leyou.bid.service.BidService;
import com.leyou.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("bid")
public class BidController {
    @Autowired
    private BidService bidService;

    @GetMapping("vender/page")
    public ResponseEntity<PageResult<Bid>> queryVenderByPage(
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "uid" ,required = true) Long uid,
            @RequestParam(value = "key", required = false) String key
    ){
        return ResponseEntity.ok(bidService.queryVenderByPage(page, rows, uid, key));
    }

    @GetMapping("admin/page")
    public ResponseEntity<PageResult<Bid>> queryAdminByPage(
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy" ,required = false) String sortBy,
            @RequestParam(value = "desc" ,defaultValue = "false") Boolean desc,
            @RequestParam(value = "key" , required = false) String key
    ){
        return ResponseEntity.ok(bidService.queryAdminByPage(page, rows, sortBy, desc, key));
    }

    @PutMapping("admin/state/{bidid}")
    public ResponseEntity<Void> updateBidStateByAdmin(@PathVariable("bidid") Long bidid){
        bidService.updateTenderStateByAdmin(bidid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("vender/bid")
    //@RequestBody
    public ResponseEntity<Void> saveTender(Bid bid){
        bidService.saveBid(bid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("vender/state/remove/{bidid}")
    public ResponseEntity<Void> updateBidStateRemoveByVender(@PathVariable("bidid") Long bidid){
        bidService.updateBidStateRemoveByVender(bidid);
        return ResponseEntity.ok().build();
    }
}
