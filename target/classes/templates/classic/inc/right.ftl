<#--音乐播放器嵌入-->
<#--<div class="panel panel-default widget">-->
<#--	<iframe src="http://music.163.com/outchain/player?type=0&amp;id=34238509&amp;auto=0&amp;height=430" width="100%" height="450" frameborder="no" marginwidth="0" marginheight="0"></iframe>-->
<#--</div>-->
<#--<div class="panel panel-default widget">-->
<#--	<div class="panel-heading">-->
<#--		<h3 class="panel-title"><i class="fa fa-area-chart"></i> 音乐鉴赏</h3>-->
<#--	</div>-->
<#--	<div>-->
<#--		<iframe frameborder="no" border="0" marginwidth="0" marginheight="0" width=100% height=86 src="https://music.163.com/outchain/player?type=2&id=27571483&auto=1&height=66"></iframe>-->
<#--	</div>-->
<#--</div>-->

<#--	音乐推荐aplayer-->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/aplayer@1.7.0/dist/APlayer.min.css">
<script src="https://cdn.jsdelivr.net/npm/aplayer@1.7.0/dist/APlayer.min.js"></script>


<div class="panel panel-default widget">
	<div class="panel-heading">
		<h3 class="panel-title"><i class="fa fa-area-chart"></i> 音乐鉴赏</h3>
	</div>
	<div>
		<div class="aplayer" data-id="60198" data-server="netease" data-type="playlist"></div>
	</div>
</div>


<script src="https://cdn.jsdelivr.net/npm/meting@1.1.0/dist/Meting.min.js"></script>


<div class="panel panel-default widget">
	<div class="panel-heading">
		<h3 class="panel-title"><i class="fa fa-area-chart"></i> 热门文章</h3>
	</div>
	<div class="panel-body">
		<@sidebar method="hottest_posts">
		<ul class="list">
			<#list results as row>
            <li>${row_index + 1}. <a href="${base}/post/${row.id}">${row.title}</a></li>
			</#list>
		</ul>
		</@sidebar>
	</div>
</div>



<div class="panel panel-default widget">
	<div class="panel-heading">
		<h3 class="panel-title"><i class="fa fa-bars"></i> 最新发布</h3>
	</div>
	<div class="panel-body">
		<@sidebar method="latest_posts">
			<ul class="list">
				<#list results as row>
					<li>${row_index + 1}. <a href="${base}/post/${row.id}">${row.title}</a></li>
				</#list>
			</ul>
		</@sidebar>
	</div>
</div>
<@controls name="comment">
<div class="panel panel-default widget">
    <div class="panel-heading">
        <h3 class="panel-title"><i class="fa fa-comment-o"></i> 最新评论</h3>
    </div>
    <div class="panel-body">
		<@sidebar method="latest_comments">
			<ul class="list">
				<#list results as row>
					<li><a href="${base}/post/${row.postId}">${row.content}</a></li>
				</#list>
			</ul>
		</@sidebar>
    </div>
</div>
</@controls>