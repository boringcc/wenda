
$(document).ready(function(){
//登录注册页面切换
    $("#to-regist").click(function(){
            $("#windows1").hide();
            $("#windows2").show();
    })

    $("#to-login").click(function(){
        $("#windows2").hide();
        $("#windows1").show();
    })

    $("#getCode").click(function () {
        var email = $("#email").val();
        if(ismail(email)){
            $.ajax({
                type: "POST",
                url: '/sendCode',
                data: 'email='+email,
                success:function(data){
                    alert("发送成功");
                }
            })

        }else {
            alert("邮箱不正确");
        }
    })

    $("#loadMoreQuestion").click(function () {
        var userId = $("#userId").text();
        var sortType = $("#sortType").text();
        var length = $(".feed-main").length;
        $.ajax({
            type: "POST",
            url: '/loadMoreQuestion',
            data:{offset: length,sortType:sortType,userId:userId},
            success:function(data){
                var txt2 = data;
                var obj = eval ("(" + txt2 + ")");
                var count = obj.count;
                for(var i = 0;i < count ;i++){
                    var addQuestion  = $("#js-home-feed-list").html();

                    var addtex = '<div class="feed-item folding feed-item-hook feed-item-2" feed-item-a="" data-type="a" id="feed-2" data-za-module="FeedItem" data-za-index=""><meta itemprop="ZReactor" data-id="389034" data-meta="{&quot;source_type&quot;: &quot;promotion_answer&quot;, &quot;voteups&quot;: 4168, &quot;comments&quot;: 69, &quot;source&quot;: []}"><div class="feed-item-inner"><div class="avatar"><a title="'
                        +obj.users[i].name + '" data-tip="p$t$amuro1230" class="zm-item-link-avatar" target="_blank" href="https://nowcoder.com/people/amuro1230"><img src="'
                        + obj.users[i].headUrl+ '" class="zm-item-img-avatar"></a></div><div class="feed-main"><div class="feed-content" data-za-module="AnswerItem"><meta itemprop="answer-id" content="389034"><meta itemprop="answer-url-token" content="13174385"><h2 class="feed-title"><a class="question_link" target="_blank" href="/question/'
                        +obj.question[i].id + '">'
                        +obj.question[i].title+ '</a></h2><div class="feed-question-detail-item"><div class="question-description-plain zm-editable-content"></div></div><div class="expandable entry-body"><div class="zm-item-vote"><a class="zm-item-vote-count js-expand js-vote-count" href="javascript:;" data-bind-votecount="">'
                        +obj.followCount[i]+ '</a></div><div class="zm-item-answer-author-info"><a class="author-link" data-tip="p$b$amuro1230" target="_blank" href="/user/'
                        +obj.users[i].id+ '">'
                +obj.users[i].name+ '</a>， '
                + formatDateTime(obj.question[i].createdDate)+ '</div><div class="zm-item-rich-text expandable js-collapse-body" data-resourceid="123114" data-action="/answer/content" data-author-name="李淼" data-entry-url="/question/19857995/answer/13174385"><div class="zh-summary summary clearfix">'
                +obj.question[i].content+ '</div></div></div><div class="feed-meta"><div class="zm-item-meta answer-actions clearfix js-contentActions"><div class="zm-meta-panel"><a data-follow="q:link" class="follow-link zg-follow meta-item" href="javascript:;" id="sfb-123114"><i class="z-icon-follow"></i>关注问题</a><a href="#" name="addcomment" class="meta-item toggle-comment js-toggleCommentBox"><i class="z-icon-comment"></i>'
                +obj.question[i].commentCount + '条评论</a><button class="meta-item item-collapse js-collapse"><i class="z-icon-fold"></i>收起</button></div></div></div></div></div></div></div>'
                ;
                    addQuestion += addtex;
                    $("#js-home-feed-list").html(addQuestion);
                }
            }
        })
    })

    $("#loadMoreAction").click(function () {
        var userId = $("#userId").text();
        var length = $(".actionsDetail").length;
        $.ajax({
            type: "POST",
            url: '/loadMoreActions',
            data:{offset: length ,userId:userId},
            success:function(data){
                var txt2 = data;
                var obj = eval ("(" + txt2 + ")");
                var count = obj.mycount;
                var addQuestion  = $("#js-home-feed-list").html();
                for(var i = 0;i < count ;i++){
                    var addtex = "<div><a class=\"actionsDetail\" href=\""+
                                +obj.urls[i]+"\">"
                                +obj.contents[i] +"</a>，"
                                +formatDateTime(obj.createdDates[i]) + "</div>";
                    addQuestion += addtex;
                    $("#js-home-feed-list").html(addQuestion);
                }
            }
        })
    })

    function formatDateTime(timeStamp) {
        var date = new Date();
        date.setTime(timeStamp);
        var y = date.getFullYear();
        var m = date.getMonth() + 1;
        m = m < 10 ? ('0' + m) : m;
        var d = date.getDate();
        d = d < 10 ? ('0' + d) : d;
        var h = date.getHours();
        h = h < 10 ? ('0' + h) : h;
        var minute = date.getMinutes();
        var second = date.getSeconds();
        minute = minute < 10 ? ('0' + minute) : minute;
        second = second < 10 ? ('0' + second) : second;
        return y + '-' + m + '-' + d+' '+h+':'+minute+':'+second;
    };

    function ismail(mail){
        return(new RegExp(/^(?:[a-zA-Z0-9]+[_\-\+\.]?)*[a-zA-Z0-9]+@(?:([a-zA-Z0-9]+[_\-]?)*[a-zA-Z0-9]+\.)+([a-zA-Z]{2,})+$/).test(mail));
    }
})