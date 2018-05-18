package com.cc.wenda.controller;

import com.cc.wenda.model.EntityType;
import com.cc.wenda.model.Feed;
import com.cc.wenda.model.HostHolder;
import com.cc.wenda.service.FeedService;
import com.cc.wenda.service.FollowService;
import com.cc.wenda.service.UserService;
import com.cc.wenda.util.JedisAdapter;
import com.cc.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FeedController {

    @Autowired
    FeedService feedService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    JedisAdapter jedisAdapter;

    //拉
    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPullFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followees = new ArrayList<>();
        if (localUserId != 0) {
            // 关注的人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }
        List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE, followees, 10);
        //"commetnUserId":"18","commentUserName":"ee","userName":"cc","userId":"1","content":"我是ee，我觉得ok"}
        model.addAttribute("feeds", feeds);
        return "feeds";
    }

    //推
    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<String> feedIds = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId),0,10);
        List<Feed> feeds = new ArrayList<Feed>();
        for(String feedId : feedIds){
            Feed feed =  feedService.getById(Integer.parseInt(feedId));
            if(feed == null){
                continue;
            }
            feeds.add(feed);
        }
        model.addAttribute("feeds", feeds);
        return "feeds";
    }
}
