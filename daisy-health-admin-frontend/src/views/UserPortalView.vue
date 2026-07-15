<template>
  <main class="portal-page">
    <header class="portal-header">
      <div class="portal-identity">
        <div class="portal-avatar-control">
          <el-avatar :size="54" :src="assetUrl(profile.avatarUrl)">{{ profile.realName?.slice(0, 1) || '用' }}</el-avatar>
          <el-button link type="primary" @click="openAvatarEditor">修改头像</el-button>
        </div>
        <div>
        <p>用户端</p>
        <h1>{{ profile.realName || auth.user?.name || '我的健康' }}</h1>
        </div>
      </div>
      <div class="portal-header-actions">
        <el-button type="primary" plain @click="openMessageDialog">给管理员留言</el-button>
        <el-button @click="passwordVisible = true">修改密码</el-button>
        <el-button class="mobile-logout-button" type="danger" plain :loading="loggingOut" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </header>

    <el-alert v-if="error" :title="error" type="error" show-icon />

    <section class="summary-grid">
      <article class="summary-card green">
        <span>当前积分</span>
        <strong>{{ points.points ?? 0 }}</strong>
        <small>{{ points.level || '普通' }}</small>
      </article>
      <article class="summary-card coral">
        <span>我的订单</span>
        <strong>{{ orders.length }}</strong>
        <small>只显示本人订单</small>
      </article>
      <article class="summary-card blue">
        <span>绑定设备</span>
        <strong>{{ devices.length }}</strong>
        <small>只显示本人设备</small>
      </article>
      <article class="summary-card purple">
        <span>我的工单</span>
        <strong>{{ workOrders.length }}</strong>
        <small>只显示本人工单</small>
      </article>
      <button type="button" class="summary-card ai-assistant-card" @click="goToAiChat">
        <el-icon><Service /></el-icon>
        <span>AI 智能客服</span>
        <strong>立即咨询</strong>
        <small>项目帮助与健康记录解读</small>
      </button>
    </section>

    <el-tabs v-model="activeTab" class="portal-tabs">
      <el-tab-pane label="个人资料" name="profile">
        <section class="panel">
          <div class="panel-heading">
            <h2>完整个人信息</h2>
            <el-button type="primary" @click="openProfileEditor">编辑资料</el-button>
          </div>
          <el-descriptions :column="profileColumns" border>
            <el-descriptions-item label="昵称">{{ profile.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ profile.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ profile.birthday }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ profile.idCard || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ profile.address }}</el-descriptions-item>
            <el-descriptions-item label="民族">{{ profile.ethnicity || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文化程度">{{ profile.education || '-' }}</el-descriptions-item>
            <el-descriptions-item label="身高">{{ profile.height ? `${profile.height} 厘米` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ profile.weight ? `${profile.weight} 千克` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="血型">{{ profile.bloodType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="RH阴性">{{ profile.rhNegative ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="慢病">{{ profile.chronicDisease }}</el-descriptions-item>
            <el-descriptions-item label="睡眠质量">{{ profile.sleepQuality || '-' }}</el-descriptions-item>
            <el-descriptions-item label="吸烟频率">{{ profile.smokingFreq || '-' }}</el-descriptions-item>
            <el-descriptions-item label="饮酒频率">{{ profile.drinkingFreq || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运动频率">{{ profile.exerciseFreq || '-' }}</el-descriptions-item>
            <el-descriptions-item label="饮食偏好">{{ profile.dietPreference || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ profile.emergencyContact || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急电话">{{ profile.emergencyPhone }}</el-descriptions-item>
            <el-descriptions-item label="个人简介" :span="profileColumns">{{ profile.bio || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </el-tab-pane>
      <el-tab-pane label="社区活动" name="activities">
        <div class="activity-section-heading">
          <div><h2>我的活动</h2><p>查看已经报名或参加过的活动</p></div>
          <div class="activity-heading-actions">
            <el-tag type="success" size="large">{{ myActivities.length }} 个活动</el-tag>
            <el-button :loading="activityRefreshing" @click="refreshActivities">刷新活动</el-button>
          </div>
        </div>
        <paged-list :items="myActivities" v-slot="{ items }">
        <section class="content-grid">
          <article v-for="item in items" :key="`mine-${item.id}`" class="portal-content-card activity-card-mine">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
            <div class="content-card-body">
              <div class="content-meta"><el-tag type="success">{{ item.enrollmentStatus || '已报名' }}</el-tag><span>{{ item.startTime }}</span></div>
              <h2>{{ item.title }}</h2>
              <p class="activity-summary">{{ item.content || '欢迎参加社区活动' }}</p>
              <p><strong>地点：</strong>{{ item.location || '待通知' }}</p>
              <footer>
                <span>{{ item.enrollTime ? `报名于 ${item.enrollTime}` : `已报名 ${item.enrolled} / ${item.quota} 人` }}</span>
                <div class="activity-card-actions">
                  <el-button type="danger" plain size="large" :loading="cancellingActivityId === item.id" @click="cancelActivity(item)">取消报名</el-button>
                  <el-button type="primary" plain size="large" @click="openActivity(item)">查看详情</el-button>
                </div>
              </footer>
            </div>
          </article>
          <el-empty v-if="!myActivities.length" description="您还没有参加活动，可在下方选择报名" />
        </section>
        </paged-list>

        <el-divider />
        <div class="activity-section-heading">
          <div><h2>可选择的活动</h2><p>活动内容由管理端统一发布并实时同步</p></div>
        </div>
        <paged-list :items="availableActivities" v-slot="{ items }">
        <section class="content-grid">
          <article v-for="item in items" :key="`available-${item.id}`" class="portal-content-card">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
            <div class="content-card-body">
              <div class="content-meta"><el-tag>{{ item.status }}</el-tag><span>{{ item.startTime }}</span></div>
              <h2>{{ item.title }}</h2>
              <p class="activity-summary">{{ item.content || '欢迎参加社区活动' }}</p>
              <p><strong>地点：</strong>{{ item.location || '待通知' }}</p>
              <footer>
                <span>已报名 {{ item.enrolled }} / {{ item.quota }} 人</span>
                <div class="activity-card-actions">
                  <el-button plain size="large" @click="openActivity(item)">活动详情</el-button>
                  <el-button type="primary" size="large" :loading="enrollingActivityId === item.id" :disabled="!item.canJoin" @click="joinActivity(item)">
                    {{ item.canJoin ? '加入活动' : '不可报名' }}
                  </el-button>
                </div>
              </footer>
            </div>
          </article>
          <el-empty v-if="!availableActivities.length" description="暂无可报名活动" />
        </section>
        </paged-list>

        <template v-if="unavailableActivities.length">
          <el-divider />
          <div class="activity-section-heading">
            <div><h2>往期或暂不可报名</h2><p>仍可查看活动详情，已结束或名额已满的活动不能报名</p></div>
          </div>
          <paged-list :items="unavailableActivities" v-slot="{ items }">
          <section class="content-grid">
            <article v-for="item in items" :key="`unavailable-${item.id}`" class="portal-content-card activity-card-muted">
              <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
              <div class="content-card-body">
                <div class="content-meta"><el-tag type="info">{{ item.enrollmentStatus || item.status }}</el-tag><span>{{ item.startTime }}</span></div>
                <h2>{{ item.title }}</h2>
                <p class="activity-summary">{{ item.content || '欢迎参加社区活动' }}</p>
                <p><strong>地点：</strong>{{ item.location || '待通知' }}</p>
                <footer><span>已报名 {{ item.enrolled }} / {{ item.quota }} 人</span><el-button plain size="large" @click="openActivity(item)">查看详情</el-button></footer>
              </div>
            </article>
          </section>
          </paged-list>
        </template>
      </el-tab-pane>
      <el-tab-pane label="健康资讯" name="articles">
        <paged-list :items="healthArticles" v-slot="{ items }">
        <section class="content-grid">
          <article v-for="item in items" :key="item.id" class="portal-content-card">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
            <div class="content-card-body">
              <div class="content-meta"><el-tag type="success">{{ item.category || '健康' }}</el-tag><span>{{ item.createdAt }}</span></div>
              <h2>{{ item.title }}</h2>
              <p>{{ item.summary || item.content || '暂无摘要' }}</p>
              <footer><small>{{ item.author || '健康中心' }}</small><el-button type="primary" plain size="large" @click="openArticle(item)">阅读详情</el-button></footer>
            </div>
          </article>
          <el-empty v-if="!healthArticles.length" description="暂无健康资讯" />
        </section>
        </paged-list>
      </el-tab-pane>
      <el-tab-pane label="健康讲堂" name="videos">
        <paged-list :items="healthVideos" v-slot="{ items }">
        <section class="content-grid">
          <article v-for="item in items" :key="item.id" class="portal-content-card">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
            <div class="content-card-body">
              <div class="content-meta"><el-tag type="warning">{{ item.category || '健康讲堂' }}</el-tag><span>{{ formatDuration(item.duration) }}</span></div>
              <h2>{{ item.title }}</h2>
              <p>{{ item.description || '暂无讲堂简介' }}</p>
              <footer><span>主讲：{{ item.lecturer || '健康专家' }}</span><el-button v-if="item.videoUrl" type="primary" size="large" @click="openVideo(item.videoUrl)">观看讲堂</el-button></footer>
            </div>
          </article>
          <el-empty v-if="!healthVideos.length" description="暂无健康讲堂" />
        </section>
        </paged-list>
      </el-tab-pane>
      <el-tab-pane label="商品服务" name="catalog">
        <paged-list :items="catalogItems" v-slot="{ items }">
        <section class="catalog-grid">
          <article v-for="item in items" :key="item.id" class="catalog-card">
            <div>
              <el-tag size="small">{{ item.itemType || '服务' }}</el-tag>
              <span>{{ item.category }}</span>
            </div>
            <h2>{{ item.name }}</h2>
            <p>{{ item.description || '暂无说明' }}</p>
            <footer>
              <strong>¥{{ item.price }}</strong>
              <small v-if="item.duration">{{ item.duration }} 分钟</small>
              <el-button type="primary" @click="openWorkOrder(item)">创建工单</el-button>
            </footer>
          </article>
        </section>
        </paged-list>
      </el-tab-pane>
      <el-tab-pane label="我的工单" name="workOrders">
        <paged-list :items="workOrders" v-slot="{ items }">
        <el-table :data="items" stripe>
          <el-table-column prop="orderNo" label="工单编号" min-width="160" />
          <el-table-column prop="serviceItem" label="服务项目" min-width="160" />
          <el-table-column prop="customerName" label="客户" min-width="110" />
          <el-table-column prop="personnelName" label="服务人员" min-width="110" />
          <el-table-column prop="amount" label="金额" min-width="80" />
          <el-table-column prop="dispatchTime" label="派单时间" min-width="160" />
          <el-table-column prop="serviceTime" label="服务时间" min-width="160" />
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="statusTone(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="150" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === '待服务'">
                <el-button link type="primary" @click="openReschedule(row)">改期</el-button>
                <el-button link type="danger" :loading="cancellingWorkOrderId === row.id" @click="cancelWorkOrder(row)">取消</el-button>
              </template>
              <span v-else class="muted-action">不可修改</span>
            </template>
          </el-table-column>
        </el-table>
        </paged-list>
      </el-tab-pane>
      <el-tab-pane label="服务评价" name="reviews">
        <paged-list :items="reviews" v-slot="{ items }">
          <el-table :data="items" stripe>
            <el-table-column prop="orderNo" label="订单编号" min-width="160" />
            <el-table-column prop="serviceItem" label="服务项目" min-width="150" />
            <el-table-column prop="personnelName" label="服务人员" min-width="110" />
            <el-table-column prop="completeTime" label="完成时间" min-width="160" />
            <el-table-column label="我的评分" min-width="170">
              <template #default="{ row }">
                <el-rate v-if="row.reviewed" :model-value="Number(row.rating)" disabled show-score score-template="{value} 星" />
                <el-tag v-else type="warning">待评价</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" min-width="220" />
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button v-if="!row.reviewed" link type="primary" @click="openReview(row)">评价服务</el-button>
                <span v-else class="muted-action">已评价</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!reviews.length" description="暂无已完成的服务" />
        </paged-list>
      </el-tab-pane>
      <el-tab-pane label="健康数据" name="health">
        <section class="panel">
          <div ref="userHealthChart" class="health-chart"></div>
        </section>
        <data-table :rows="healthData" :columns="healthColumns" />
      </el-tab-pane>
      <el-tab-pane label="用药记录" name="medications">
        <data-table :rows="medications" :columns="medicationColumns" />
      </el-tab-pane>
      <el-tab-pane label="设备报告" name="devices">
        <section class="panel">
          <h2>我的设备</h2>
          <paged-list :items="devices" v-slot="{ items }">
          <el-table :data="items" stripe>
            <el-table-column v-for="column in deviceColumns" :key="column.prop" :prop="column.prop" :label="column.label" :min-width="column.width || 120">
              <template #default="{ row }">{{ localizeValue(row[column.prop]) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template #default="{ row }"><el-button link type="primary" @click="openDeviceEditor(row)">编辑</el-button></template>
            </el-table-column>
          </el-table>
          </paged-list>
        </section>
        <data-table :rows="reports" :columns="reportColumns" title="健康报告" />
      </el-tab-pane>
      <el-tab-pane label="订单资产" name="assets">
        <data-table :rows="orders" :columns="orderColumns" />
        <data-table :rows="coupons" :columns="couponColumns" title="优惠券" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="avatarDialogVisible" title="修改头像" width="680px">
      <avatar-picker ref="avatarPickerRef" :model-value="profile.avatarUrl" :fallback="profile.realName?.slice(0, 1) || '用'" />
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="avatarSaving" @click="saveAvatar">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="profileDialogVisible" title="编辑完整个人信息" width="720px">
      <el-form :model="profileForm" label-width="104px">
        <el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item>
        <el-form-item label="真实姓名" required><el-input v-model="profileForm.realName" /></el-form-item>
        <el-form-item label="手机号" required><el-input v-model="profileForm.phone" /></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="profileForm.gender"><el-radio label="女" /><el-radio label="男" /><el-radio label="未知" /></el-radio-group></el-form-item>
        <el-form-item label="出生日期"><el-date-picker v-model="profileForm.birthday" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="profileForm.idCard" /></el-form-item>
        <el-form-item label="家庭住址"><el-input v-model="profileForm.address" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="profileForm.bio" type="textarea" /></el-form-item>
        <el-form-item label="民族"><el-input v-model="profileForm.ethnicity" /></el-form-item>
        <el-form-item label="文化程度"><el-input v-model="profileForm.education" /></el-form-item>
        <el-form-item label="身高（厘米）"><el-input-number v-model="profileForm.height" :min="0" /></el-form-item>
        <el-form-item label="体重（千克）"><el-input-number v-model="profileForm.weight" :min="0" /></el-form-item>
        <el-form-item label="血型"><el-select v-model="profileForm.bloodType" clearable><el-option v-for="type in ['A', 'B', 'O', 'AB']" :key="type" :label="type" :value="type" /></el-select></el-form-item>
        <el-form-item label="RH阴性"><el-switch v-model="profileForm.rhNegative" /></el-form-item>
        <el-form-item label="慢性病"><el-input v-model="profileForm.chronicDisease" /></el-form-item>
        <el-form-item label="睡眠质量"><el-select v-model="profileForm.sleepQuality" clearable><el-option v-for="item in ['良好', '一般', '较差']" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="吸烟频率"><el-input v-model="profileForm.smokingFreq" /></el-form-item>
        <el-form-item label="饮酒频率"><el-input v-model="profileForm.drinkingFreq" /></el-form-item>
        <el-form-item label="运动频率"><el-input v-model="profileForm.exerciseFreq" /></el-form-item>
        <el-form-item label="饮食偏好"><el-input v-model="profileForm.dietPreference" /></el-form-item>
        <el-form-item label="紧急联系人"><el-input v-model="profileForm.emergencyContact" /></el-form-item>
        <el-form-item label="紧急电话"><el-input v-model="profileForm.emergencyPhone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存并同步</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deviceDialogVisible" title="编辑设备信息" width="560px">
      <el-form :model="deviceForm" label-width="96px">
        <el-form-item label="设备名称" required><el-input v-model="deviceForm.deviceName" /></el-form-item>
        <el-form-item label="设备类型"><el-input v-model="deviceForm.deviceType" /></el-form-item>
        <el-form-item label="设备编号"><el-input v-model="deviceForm.deviceCode" /></el-form-item>
        <el-form-item label="绑定状态"><el-select v-model="deviceForm.status"><el-option label="绑定" value="绑定" /><el-option label="解绑" value="解绑" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deviceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="deviceSaving" @click="saveDevice">保存并同步</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="articleDialogVisible" :title="selectedArticle.title || '健康资讯'" width="760px">
      <div class="article-detail-meta">{{ selectedArticle.author || '健康中心' }} · {{ selectedArticle.createdAt || '' }}</div>
      <p v-if="selectedArticle.summary" class="article-summary">{{ selectedArticle.summary }}</p>
      <div class="article-content">{{ selectedArticle.content || selectedArticle.summary || '暂无正文' }}</div>
      <template #footer><el-button type="primary" size="large" @click="articleDialogVisible = false">我知道了</el-button></template>
    </el-dialog>

    <el-dialog v-model="activityDialogVisible" :title="selectedActivity.title || '活动详情'" width="760px">
      <img v-if="selectedActivity.coverUrl" class="activity-detail-cover" :src="assetUrl(selectedActivity.coverUrl)" :alt="selectedActivity.title" />
      <el-descriptions :column="detailColumns" border class="activity-detail-info">
        <el-descriptions-item label="活动状态">{{ selectedActivity.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参加状态">{{ selectedActivity.enrollmentStatus || (selectedActivity.joined ? '已报名' : '未报名') }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ selectedActivity.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ selectedActivity.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="活动地点" :span="detailColumns">{{ selectedActivity.location || '待通知' }}</el-descriptions-item>
        <el-descriptions-item label="报名人数">{{ selectedActivity.enrolled || 0 }} / {{ selectedActivity.quota || 0 }}</el-descriptions-item>
        <el-descriptions-item label="我的报名时间">{{ selectedActivity.enrollTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="activity-detail-content">
        <h3>活动介绍</h3>
        <p>{{ selectedActivity.content || '暂无活动介绍' }}</p>
      </div>
      <template #footer>
        <el-button size="large" @click="activityDialogVisible = false">关闭</el-button>
        <el-button
          v-if="!selectedActivity.joined"
          type="primary"
          size="large"
          :loading="enrollingActivityId === selectedActivity.id"
          :disabled="!selectedActivity.canJoin"
          @click="joinActivity(selectedActivity)"
        >{{ selectedActivity.canJoin ? '加入活动' : '不可报名' }}</el-button>
        <el-button
          v-else-if="selectedActivity.enrollmentStatus === '已报名'"
          type="danger"
          plain
          size="large"
          :loading="cancellingActivityId === selectedActivity.id"
          @click="cancelActivity(selectedActivity)"
        >取消报名</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="workOrderVisible" title="创建工单" width="560px">
      <el-form :model="workOrderForm" label-width="96px">
        <el-form-item label="商品服务" required>
          <el-select v-model="workOrderForm.productId" filterable @change="syncWorkOrderAmount">
            <el-option v-for="item in catalogItems" :key="item.id" :label="`${item.name} · ¥${item.price}`" :value="String(item.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务人员" required>
          <el-select v-model="workOrderForm.personnelId" filterable placeholder="请选择服务人员">
            <el-option v-for="item in personnelOptions" :key="item.id" :label="`${item.name}${item.serviceType ? ` · ${item.serviceType}` : ''}`" :value="String(item.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="workOrderForm.amount" :min="0" disabled /></el-form-item>
        <el-form-item label="服务日期"><el-date-picker v-model="workOrderForm.date" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="服务时间"><el-time-picker v-model="workOrderForm.time" value-format="HH:mm:ss" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workOrderVisible = false">取消</el-button>
        <el-button type="primary" :loading="workOrderSaving" @click="submitWorkOrder">提交工单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rescheduleVisible" title="工单改期" width="520px">
      <el-form :model="rescheduleForm" label-width="88px">
        <el-form-item label="工单编号"><span>{{ rescheduleForm.orderNo }}</span></el-form-item>
        <el-form-item label="服务日期" required><el-date-picker v-model="rescheduleForm.date" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="服务时间" required><el-time-picker v-model="rescheduleForm.time" value-format="HH:mm:ss" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rescheduleVisible = false">取消</el-button>
        <el-button type="primary" :loading="rescheduleSaving" @click="submitReschedule">确认改期</el-button>
      </template>
    </el-dialog>

    <admin-message-dialog v-model="messageVisible" />
    <el-dialog v-model="reviewVisible" title="评价服务" width="560px">
      <el-form label-position="top">
        <el-form-item label="服务项目"><strong>{{ reviewForm.serviceItem }}</strong></el-form-item>
        <el-form-item label="服务人员"><span>{{ reviewForm.personnelName || '未指定' }}</span></el-form-item>
        <el-form-item label="服务评分" required><el-rate v-model="reviewForm.rating" size="large" show-text /></el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="请填写您对本次服务的评价" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSaving" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
    <password-change-dialog v-model="passwordVisible" />
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import AvatarPicker from '../components/AvatarPicker.vue'
import AdminMessageDialog from '../components/AdminMessageDialog.vue'
import PasswordChangeDialog from '../components/PasswordChangeDialog.vue'
import PagedList from '../components/PagedList.vue'
import { createHealthChartOption } from '../utils/healthChart'
import { useResponsiveColumns } from '../utils/viewport'
import { splitUserActivities } from '../utils/activity'
import { localizeValue } from '../utils/localizeValue'
import { useAuthStore } from '../stores/auth'
import {
  assetUrl,
  cancelElderlyActivity,
  cancelElderlyWorkOrder,
  createElderlyReview,
  createElderlyWorkOrder,
  enrollElderlyActivity,
  getElderlyActivities,
  getElderlyCatalogItems,
  getElderlyCoupons,
  getElderlyDevices,
  getElderlyHealthData,
  getElderlyHealthArticles,
  getElderlyHealthVideos,
  getElderlyMedications,
  getElderlyOrders,
  getElderlyReviews,
  getElderlyPersonnel,
  getElderlyPoints,
  getElderlyProfile,
  getElderlyReports,
  getElderlyWorkOrders,
  rescheduleElderlyWorkOrder,
  updateElderlyAvatar,
  updateElderlyDevice,
  updateElderlyProfile
} from '../api/http'
import { openExternalUrl } from '../native/app.js'

const DataTable = {
  components: { PagedList },
  methods: { localizeValue },
  props: { rows: { type: Array, default: () => [] }, columns: { type: Array, default: () => [] }, title: String },
  template: `
    <section class="panel">
      <h2 v-if="title">{{ title }}</h2>
      <paged-list :items="rows" v-slot="{ items }">
        <el-table :data="items" stripe>
          <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" :min-width="column.width || 120">
            <template #default="{ row }">{{ localizeValue(row[column.prop]) }}</template>
          </el-table-column>
        </el-table>
      </paged-list>
    </section>
  `
}

const router = useRouter()
const auth = useAuthStore()
const profileColumns = useResponsiveColumns(3)
const detailColumns = useResponsiveColumns(2)
const loggingOut = ref(false)
const activeTab = ref('profile')
const error = ref('')
const passwordVisible = ref(false)
const profile = ref({})
const healthData = ref([])
const userHealthChart = ref(null)
const medications = ref([])
const devices = ref([])
const reports = ref([])
const orders = ref([])
const coupons = ref([])
const points = ref({})
const catalogItems = ref([])
const personnelOptions = ref([])
const workOrders = ref([])
const reviews = ref([])
const activities = ref([])
const activityGroups = computed(() => splitUserActivities(activities.value))
const myActivities = computed(() => activityGroups.value.mine)
const availableActivities = computed(() => activityGroups.value.available)
const unavailableActivities = computed(() => activityGroups.value.unavailable)
const healthArticles = ref([])
const healthVideos = ref([])
const enrollingActivityId = ref(null)
const cancellingActivityId = ref(null)
const cancellingWorkOrderId = ref(null)
const activityRefreshing = ref(false)
const articleDialogVisible = ref(false)
const selectedArticle = ref({})
const activityDialogVisible = ref(false)
const selectedActivity = ref({})
const workOrderVisible = ref(false)
const workOrderSaving = ref(false)
const rescheduleVisible = ref(false)
const rescheduleSaving = ref(false)
const rescheduleForm = reactive({ id: null, orderNo: '', date: '', time: '09:00:00' })
const messageVisible = ref(false)
const reviewVisible = ref(false)
const reviewSaving = ref(false)
const reviewForm = reactive({ orderId: null, serviceItem: '', personnelName: '', rating: 5, content: '' })
const avatarDialogVisible = ref(false)
const avatarSaving = ref(false)
const avatarPickerRef = ref(null)
const profileDialogVisible = ref(false)
const profileSaving = ref(false)
const profileForm = reactive({
  nickname: '', realName: '', phone: '', gender: '未知', birthday: '', idCard: '', address: '', bio: '',
  height: null, weight: null, ethnicity: '', education: '', bloodType: '', rhNegative: false,
  chronicDisease: '', sleepQuality: '', smokingFreq: '', drinkingFreq: '', exerciseFreq: '', dietPreference: '',
  emergencyContact: '', emergencyPhone: ''
})
const deviceDialogVisible = ref(false)
const deviceSaving = ref(false)
const deviceForm = reactive({ id: null, deviceName: '', deviceType: '', deviceCode: '', status: '绑定' })
const today = new Date().toISOString().slice(0, 10)
const workOrderForm = reactive({ productId: '', personnelId: '', amount: 0, date: today, time: '09:00:00' })

const healthColumns = [
  { prop: 'dataType', label: '类型' },
  { prop: 'value', label: '数值' },
  { prop: 'unit', label: '单位' },
  { prop: 'recordDate', label: '日期' },
  { prop: 'recordTime', label: '时间' },
  { prop: 'source', label: '来源' }
]
const medicationColumns = [
  { prop: 'period', label: '时段' },
  { prop: 'drugName', label: '药品' },
  { prop: 'frequency', label: '频次' },
  { prop: 'takeTime', label: '时间' },
  { prop: 'dosage', label: '剂量' }
]
const deviceColumns = [
  { prop: 'deviceName', label: '设备' },
  { prop: 'deviceType', label: '类型' },
  { prop: 'deviceCode', label: '编号' },
  { prop: 'lastSyncTime', label: '同步时间' },
  { prop: 'status', label: '状态' }
]
const reportColumns = [
  { prop: 'title', label: '报告' },
  { prop: 'reportType', label: '类型' },
  { prop: 'reportDate', label: '日期' },
  { prop: 'doctorName', label: '医生' },
  { prop: 'summary', label: '摘要', width: 220 }
]
const orderColumns = [
  { prop: 'orderNo', label: '订单号' },
  { prop: 'productName', label: '项目' },
  { prop: 'amount', label: '金额' },
  { prop: 'serviceType', label: '服务类型' },
  { prop: 'status', label: '状态' },
  { prop: 'serviceTime', label: '服务时间' }
]
const couponColumns = [
  { prop: 'couponNo', label: '券编号' },
  { prop: 'name', label: '名称' },
  { prop: 'discount', label: '优惠' },
  { prop: 'minAmount', label: '门槛' },
  { prop: 'status', label: '状态' },
  { prop: 'expireDate', label: '有效期' }
]
function statusTone(status) {
  return { '待服务': 'warning', '服务中': 'primary', '已完成': 'success', '已取消': 'info' }[status] || ''
}

async function loadData() {
  try {
    const [profileData, health, medicationData, deviceData, reportData, orderData, reviewData, couponData, pointData, catalogData, workOrderData, activityData, articleData, videoData, personnelData] = await Promise.all([
      getElderlyProfile(),
      getElderlyHealthData(),
      getElderlyMedications(),
      getElderlyDevices(),
      getElderlyReports(),
      getElderlyOrders(),
      getElderlyReviews(),
      getElderlyCoupons(),
      getElderlyPoints(),
      getElderlyCatalogItems(),
      getElderlyWorkOrders(),
      getElderlyActivities(),
      getElderlyHealthArticles(),
      getElderlyHealthVideos(),
      getElderlyPersonnel()
    ])
    profile.value = profileData
    healthData.value = health
    medications.value = medicationData
    devices.value = deviceData
    reports.value = reportData
    orders.value = orderData
    reviews.value = reviewData
    coupons.value = couponData
    points.value = pointData
    catalogItems.value = catalogData
    workOrders.value = workOrderData
    activities.value = activityData
    healthArticles.value = articleData
    healthVideos.value = videoData
    personnelOptions.value = personnelData
    if (activeTab.value === 'health') {
      await nextTick()
      drawUserHealthChart()
    }
  } catch (err) {
    error.value = err.message || '加载失败'
  }
}

function openReview(row) {
  Object.assign(reviewForm, {
    orderId: row.orderId,
    serviceItem: row.serviceItem || row.productName,
    personnelName: row.personnelName || '',
    rating: 5,
    content: ''
  })
  reviewVisible.value = true
}

async function submitReview() {
  if (!reviewForm.rating) {
    ElMessage.warning('请选择服务评分')
    return
  }
  reviewSaving.value = true
  try {
    await createElderlyReview({ orderId: reviewForm.orderId, rating: reviewForm.rating, content: reviewForm.content.trim() })
    reviews.value = await getElderlyReviews()
    reviewVisible.value = false
    ElMessage.success('评价已提交，并已同步到管理端')
  } catch (err) {
    ElMessage.error(err.message || '评价提交失败')
  } finally {
    reviewSaving.value = false
  }
}

function openWorkOrder(item) {
  Object.assign(workOrderForm, { productId: String(item.id), personnelId: '', amount: Number(item.price || 0), date: today, time: '09:00:00' })
  workOrderVisible.value = true
}

function syncWorkOrderAmount(productId) {
  const selected = catalogItems.value.find((item) => String(item.id) === String(productId))
  workOrderForm.amount = Number(selected?.price || 0)
}

async function submitWorkOrder() {
  if (!workOrderForm.productId) {
    ElMessage.warning('请选择商品服务')
    return
  }
  if (!workOrderForm.personnelId) {
    ElMessage.warning('请选择服务人员')
    return
  }
  workOrderSaving.value = true
  try {
    await createElderlyWorkOrder({
      productId: workOrderForm.productId,
      personnelId: workOrderForm.personnelId,
      serviceTime: `${workOrderForm.date} ${workOrderForm.time}`
    })
    workOrders.value = await getElderlyWorkOrders()
    activeTab.value = 'workOrders'
    workOrderVisible.value = false
    ElMessage.success('工单已创建，派单时间已自动填写')
  } catch (err) {
    ElMessage.error(err.message || '工单创建失败')
  } finally {
    workOrderSaving.value = false
  }
}

function openAvatarEditor() {
  avatarDialogVisible.value = true
  nextTick(() => avatarPickerRef.value?.reset(profile.value.avatarUrl))
}

async function saveAvatar() {
  avatarSaving.value = true
  try {
    const avatarUrl = await avatarPickerRef.value?.commitSelection()
    if (!avatarUrl) {
      ElMessage.warning('请选择一个头像')
      return
    }
    profile.value = await updateElderlyAvatar({ avatarUrl })
    auth.updateCachedAvatar(profile.value.avatarUrl)
    avatarDialogVisible.value = false
    ElMessage.success('头像已更新并同步到管理端')
  } catch (err) {
    ElMessage.error(err.message || '头像保存失败')
  } finally {
    avatarSaving.value = false
  }
}

function drawUserHealthChart() {
  if (!userHealthChart.value) return
  echarts.getInstanceByDom(userHealthChart.value)?.dispose()
  echarts.init(userHealthChart.value).setOption(createHealthChartOption(healthData.value))
}

function openProfileEditor() {
  Object.keys(profileForm).forEach((key) => {
    profileForm[key] = profile.value[key] ?? (key === 'rhNegative' ? false : '')
  })
  profileDialogVisible.value = true
}

async function saveProfile() {
  if (!String(profileForm.realName || '').trim() || !String(profileForm.phone || '').trim()) {
    ElMessage.warning('请填写真实姓名和手机号')
    return
  }
  profileSaving.value = true
  try {
    profile.value = await updateElderlyProfile({ ...profileForm })
    auth.updateCachedAvatar(profile.value.avatarUrl)
    profileDialogVisible.value = false
    ElMessage.success('个人信息已保存并同步到管理端')
  } catch (err) {
    ElMessage.error(err.message || '个人信息保存失败')
  } finally {
    profileSaving.value = false
  }
}

function openDeviceEditor(row) {
  Object.assign(deviceForm, {
    id: row.id,
    deviceName: row.deviceName || '',
    deviceType: row.deviceType || '',
    deviceCode: row.deviceCode || '',
    status: row.status || '绑定'
  })
  deviceDialogVisible.value = true
}

async function saveDevice() {
  if (!String(deviceForm.deviceName || '').trim()) {
    ElMessage.warning('请填写设备名称')
    return
  }
  deviceSaving.value = true
  try {
    await updateElderlyDevice(deviceForm.id, { ...deviceForm })
    devices.value = await getElderlyDevices()
    deviceDialogVisible.value = false
    ElMessage.success('设备信息已保存并同步到管理端')
  } catch (err) {
    ElMessage.error(err.message || '设备信息保存失败')
  } finally {
    deviceSaving.value = false
  }
}

async function joinActivity(item) {
  enrollingActivityId.value = item.id
  try {
    await enrollElderlyActivity(item.id)
  } catch (err) {
    ElMessage.error(err.message || '活动报名失败')
    enrollingActivityId.value = null
    return
  }

  const optimisticActivity = {
    ...item,
    joined: true,
    canJoin: false,
    enrollmentStatus: '已报名',
    enrollTime: new Date().toLocaleString('zh-CN', { hour12: false }).replaceAll('/', '-'),
    enrolled: Number(item.enrolled || 0) + 1
  }
  activities.value = activities.value.map((activity) => String(activity.id) === String(item.id) ? optimisticActivity : activity)
  if (activityDialogVisible.value) selectedActivity.value = optimisticActivity

  try {
    activities.value = await getElderlyActivities()
    if (activityDialogVisible.value) {
      selectedActivity.value = activities.value.find((activity) => String(activity.id) === String(item.id)) || optimisticActivity
    }
    ElMessage.success(`已报名“${item.title}”`)
  } catch (err) {
    ElMessage.warning('报名已成功，活动列表暂未同步，请稍后刷新')
  } finally {
    enrollingActivityId.value = null
  }
}

async function cancelActivity(item) {
  try {
    await ElMessageBox.confirm(`确认取消“${item.title}”的报名吗？`, '取消活动报名', { type: 'warning' })
  } catch (action) {
    return
  }
  cancellingActivityId.value = item.id
  try {
    await cancelElderlyActivity(item.id)
    activities.value = await getElderlyActivities()
    if (activityDialogVisible.value) activityDialogVisible.value = false
    ElMessage.success('活动报名已取消')
  } catch (err) {
    ElMessage.error(err.message || '取消报名失败')
  } finally {
    cancellingActivityId.value = null
  }
}

async function cancelWorkOrder(row) {
  let reason = ''
  try {
    const result = await ElMessageBox.prompt('可填写取消原因', `取消工单 ${row.orderNo}`, {
      inputPlaceholder: '选填，最多200字',
      inputValidator: (value) => String(value || '').length <= 200 || '取消原因不能超过200字',
      confirmButtonText: '确认取消',
      cancelButtonText: '暂不取消',
      type: 'warning'
    })
    reason = result.value || ''
  } catch (action) {
    return
  }
  cancellingWorkOrderId.value = row.id
  try {
    await cancelElderlyWorkOrder(row.id, { reason })
    workOrders.value = await getElderlyWorkOrders()
    orders.value = await getElderlyOrders()
    ElMessage.success('工单已取消')
  } catch (err) {
    ElMessage.error(err.message || '工单取消失败')
  } finally {
    cancellingWorkOrderId.value = null
  }
}

function openReschedule(row) {
  const current = String(row.serviceTime || '').split(' ')
  Object.assign(rescheduleForm, {
    id: row.id,
    orderNo: row.orderNo,
    date: current[0] || today,
    time: current[1] ? `${current[1]}:00`.slice(0, 8) : '09:00:00'
  })
  rescheduleVisible.value = true
}

async function submitReschedule() {
  if (!rescheduleForm.date || !rescheduleForm.time) {
    ElMessage.warning('请选择新的服务日期和时间')
    return
  }
  rescheduleSaving.value = true
  try {
    await rescheduleElderlyWorkOrder(rescheduleForm.id, { serviceTime: `${rescheduleForm.date} ${rescheduleForm.time}` })
    workOrders.value = await getElderlyWorkOrders()
    rescheduleVisible.value = false
    ElMessage.success('工单已改期')
  } catch (err) {
    ElMessage.error(err.message || '工单改期失败')
  } finally {
    rescheduleSaving.value = false
  }
}

function openMessageDialog() {
  messageVisible.value = true
}

function goToAiChat() {
  router.push('/portal/user/ai-chat')
}

function openActivity(item) {
  selectedActivity.value = item
  activityDialogVisible.value = true
}

async function refreshActivities() {
  activityRefreshing.value = true
  try {
    activities.value = await getElderlyActivities()
    if (activityDialogVisible.value) {
      selectedActivity.value = activities.value.find((activity) => String(activity.id) === String(selectedActivity.value.id)) || selectedActivity.value
    }
    ElMessage.success('活动信息已同步')
  } catch (err) {
    ElMessage.error(err.message || '活动信息刷新失败')
  } finally {
    activityRefreshing.value = false
  }
}

function formatDuration(seconds) {
  const minutes = Math.ceil(Number(seconds || 0) / 60)
  return minutes ? `${minutes} 分钟` : '时长待定'
}

function openVideo(url) {
  openExternalUrl(assetUrl(url))
}

function openArticle(item) {
  selectedArticle.value = item
  articleDialogVisible.value = true
}

async function logout() {
  if (loggingOut.value) return
  loggingOut.value = true
  try {
    await auth.signOut()
    await router.replace('/login')
  } finally {
    loggingOut.value = false
  }
}

watch(activeTab, async (tab) => {
  if (tab !== 'health') return
  await nextTick()
  drawUserHealthChart()
})

onBeforeUnmount(() => {
  if (userHealthChart.value) echarts.getInstanceByDom(userHealthChart.value)?.dispose()
})

onMounted(loadData)
</script>

<style scoped>
.portal-page {
  min-height: 100vh;
  padding: 28px;
  background: #f3f7f6;
}

.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.portal-header p {
  margin: 0 0 6px;
  color: #6f7e87;
}

.portal-header h1 {
  margin: 0;
  font-size: 30px;
}

.portal-identity {
  display: flex;
  align-items: center;
  gap: 14px;
}

.portal-avatar-control {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 2px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.summary-card {
  min-height: 124px;
  padding: 20px;
  border: 0;
  border-radius: 8px;
  color: #fff;
  text-align: left;
}

.summary-card span,
.summary-card small {
  display: block;
}

.summary-card strong {
  display: block;
  margin: 12px 0 4px;
  font-size: 34px;
}

.green { background: #159a84; }
.coral { background: #e56a54; }
.blue { background: #3578c8; }
.purple { background: #7657b6; }

.ai-assistant-card {
  cursor: pointer;
  font: inherit;
  background: linear-gradient(135deg, #16a789, #58d9b6);
  box-shadow: 0 9px 24px rgb(22 167 137 / 20%);
  transition: transform .2s ease, box-shadow .2s ease;
}

.ai-assistant-card:hover,
.ai-assistant-card:focus-visible {
  outline: 3px solid rgb(22 167 137 / 20%);
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgb(22 167 137 / 28%);
}

.ai-assistant-card > .el-icon {
  float: right;
  font-size: 34px;
}

.ai-assistant-card strong {
  font-size: 25px;
}

.portal-tabs {
  padding: 18px;
  border: 1px solid #e2eaee;
  border-radius: 8px;
  background: #fff;
}

.panel {
  margin-bottom: 16px;
}

.panel h2 {
  margin: 0 0 12px;
  font-size: 18px;
}

.portal-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.health-chart {
  width: 100%;
  height: 360px;
}

.panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-heading h2 {
  margin: 0;
}

.catalog-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.activity-section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0 18px;
}

.activity-section-heading h2,
.activity-section-heading p {
  margin: 0;
}

.activity-heading-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.activity-section-heading p {
  margin-top: 6px;
  color: #6f7e87;
}

.activity-card-mine {
  border-color: #9bd5c9;
  box-shadow: 0 6px 18px rgb(21 154 132 / 10%);
}

.activity-card-muted {
  background: #f8faf9;
  opacity: .88;
}

.activity-card-actions {
  display: flex;
  align-items: center;
}

.activity-summary {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.portal-content-card {
  overflow: hidden;
  border: 1px solid #dce8e5;
  border-radius: 12px;
  background: #fff;
}

.portal-content-card > img {
  width: 100%;
  height: 190px;
  object-fit: cover;
}

.content-card-body {
  padding: 20px;
}

.content-card-body h2 {
  margin: 14px 0 10px;
  font-size: 22px;
}

.content-card-body p {
  color: #52646e;
  font-size: 16px;
  line-height: 1.7;
}

.content-card-body footer,
.content-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.content-meta,
.content-card-body small {
  color: #788991;
}

.article-detail-meta {
  margin-bottom: 14px;
  color: #788991;
}

.article-summary {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f2f8f6;
  color: #3e5b55;
  font-size: 17px;
}

.article-content {
  white-space: pre-wrap;
  color: #354850;
  font-size: 17px;
  line-height: 1.9;
}

.activity-detail-cover {
  width: 100%;
  max-height: 300px;
  margin-bottom: 18px;
  border-radius: 10px;
  object-fit: cover;
}

.activity-detail-info {
  margin-bottom: 18px;
}

.activity-detail-content h3 {
  margin-bottom: 8px;
}

.activity-detail-content p {
  white-space: pre-wrap;
  color: #425861;
  font-size: 17px;
  line-height: 1.9;
}

.muted-action {
  color: #9aa7ad;
  font-size: 13px;
}

.catalog-card {
  display: flex;
  min-height: 210px;
  flex-direction: column;
  padding: 18px;
  border: 1px solid #e2eaee;
  border-radius: 10px;
  background: #fbfdfc;
}

.catalog-card > div {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6f7e87;
}

.catalog-card h2 {
  margin: 16px 0 8px;
  font-size: 19px;
}

.catalog-card p {
  flex: 1;
  margin: 0 0 16px;
  color: #6f7e87;
  line-height: 1.6;
}

.catalog-card footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.catalog-card footer strong {
  color: #159a84;
  font-size: 22px;
}

.catalog-card footer small {
  margin-right: auto;
  color: #6f7e87;
}

@media (max-width: 1000px) {
  .summary-grid,
  .catalog-grid,
  .content-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .portal-page { padding: 14px; }
  .summary-grid,
  .catalog-grid,
  .content-grid { grid-template-columns: 1fr; }
  .portal-header h1 { font-size: 24px; }
  .portal-tabs { padding: 10px; }
  .portal-header {
    align-items: flex-start;
    gap: 14px;
  }
  .portal-header-actions {
    align-items: stretch;
    flex-direction: column;
  }
  .portal-header-actions .mobile-logout-button {
    width: 100%;
    min-height: 44px;
    margin: 0;
    font-weight: 700;
  }
  .portal-identity {
    gap: 8px;
  }
  .portal-identity > div:not(.portal-avatar-control) {
    display: none;
  }
  .summary-card {
    min-height: 108px;
    padding: 16px;
  }
  .panel-heading,
  .activity-section-heading,
  .content-card-body footer,
  .content-meta,
  .catalog-card footer {
    align-items: flex-start;
    flex-wrap: wrap;
  }
  .activity-heading-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  .portal-tabs :deep(.el-tabs__nav-wrap) {
    padding: 0 28px;
  }
  .health-chart {
    height: 280px;
  }
}
</style>
