<template>
    <el-row style="background-color: #FFFFFF;padding: 5px 0;border-radius: 5px;">
        <el-row style="padding: 10px;margin-left: 5px;">
            <el-row>
                <el-select style="width: 100px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="userQueryDto.isLogin" placeholder="Login status">
                    <el-option v-for="item in loginStatuList" :key="item.value" :label="item.label" :value="item.value">
                    </el-option>
                </el-select>
                <el-select style="width: 100px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="userQueryDto.isWord" placeholder="Mute status">
                    <el-option v-for="item in wordStatuList" :key="item.value" :label="item.label" :value="item.value">
                    </el-option>
                </el-select>
                <el-date-picker style="width: 216px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="searchTime" type="daterange" range-separator="To" start-placeholder="Start"
                    end-placeholder="End">
                </el-date-picker>
                <el-input size="small" style="width: 166px;" v-model="userQueryDto.userName" placeholder="Username" clearable
                    @clear="handleFilterClear">
                    <el-button slot="append" @click="handleFilter" icon="el-icon-search"></el-button>
                </el-input>
                <span style="float: right;" class="edit-button" @click="add()">
                    New user
                </span>
            </el-row>
        </el-row>
        <el-row style="margin: 0 22px;border-top: 1px solid rgb(245,245,245);">
            <el-table :stripe="true" :data="tableData" style="width: 100%">
                <el-table-column prop="userAvatar" width="68" label="Avatar">
                    <template slot-scope="scope">
                        <el-avatar :size="25" :src="scope.row.userAvatar" style="margin-top: 10px;"></el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="userName" label="Name"></el-table-column>
                <el-table-column prop="userAccount" width="128" label="Account"></el-table-column>
                <el-table-column prop="userEmail" width="168" label="Email"></el-table-column>
                <el-table-column prop="userRole" width="68" label="Role">
                    <template slot-scope="scope">
                        <span>{{ scope.row.userRole === 1 ? 'Admin' : 'User' }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="isLogin" width="108" label="Banned">
                    <template slot-scope="scope">
                        <i v-if="scope.row.isLogin" style="margin-right: 5px;" class="el-icon-warning"></i>
                        <i v-else style="margin-right: 5px;color: rgb(253, 199, 50);" class="el-icon-success"></i>
                        <el-tooltip v-if="scope.row.isLogin" class="item" effect="dark"
                            content="Once banned, the account cannot log in. Only an admin can unban it."
                            placement="bottom-end">
                            <span style="text-decoration: underline;text-decoration-style: dashed;">Banned</span>
                        </el-tooltip>
                        <span v-else>Normal</span>
                    </template>
                </el-table-column>
                <el-table-column prop="isWord" width="108" label="Muted">
                    <template slot-scope="scope">
                        <i v-if="scope.row.isWord" style="margin-right: 5px;" class="el-icon-warning"></i>
                        <i v-else style="margin-right: 5px;color: rgb(253, 199, 50);" class="el-icon-success"></i>
                        <el-tooltip v-if="scope.row.isWord" class="item" effect="dark"
                            content="Once muted, the account cannot comment. Only an admin can unmute it."
                            placement="bottom-end">
                            <span style="text-decoration: underline;text-decoration-style: dashed;">Muted</span>
                        </el-tooltip>
                        <span v-else>Normal</span>
                    </template>
                </el-table-column>
                <el-table-column :sortable="true" prop="createTime" width="168" label="Registered"></el-table-column>
                <el-table-column label="Actions" width="170">
                    <template slot-scope="scope">
                        <span class="text-button" @click="handleStatus(scope.row)">Status</span>
                        <span class="text-button" @click="handleEdit(scope.row)">Edit</span>
                        <span class="text-button" @click="handleDelete(scope.row)">Delete</span>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination style="margin:10px 0;float: right;" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20]"
                :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
                :total="totalItems"></el-pagination>
        </el-row>
        <!-- 操作面板 -->
        <el-dialog :show-close="false" :visible.sync="dialogUserOperaion" width="25%">
            <div style="padding:16px 20px;">
                <el-row>
                    <p>Avatar</p>
                    <el-upload class="avatar-uploader"
                        action="http://localhost:21090/api/campus-product-sys/v1.0/file/upload" :show-file-list="false"
                        :on-success="handleAvatarSuccess">
                        <img v-if="userAvatar" :src="userAvatar" class="dialog-avatar">
                        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                    </el-upload>
                </el-row>
                <el-row>
                    <span class="dialog-hover">Username</span>
                    <input class="dialog-input" v-model="data.userName" placeholder="Username" />
                    <span class="dialog-hover">Account</span>
                    <input class="dialog-input" v-model="data.userAccount" placeholder="Account" />
                    <span class="dialog-hover">Email</span>
                    <input class="dialog-input" v-model="data.userEmail" placeholder="Email" />
                    <span class="dialog-hover">Password</span>
                    <input class="dialog-input" v-model="userPwd" type="password" placeholder="Password" />
                </el-row>
            </div>
            <span slot="footer" class="dialog-footer" style="margin-top: 10px;">
                <span class="channel-button" @click="cannel()">
                    Cancel
                </span>
                <span v-if="!isOperation" class="edit-button" @click="addOperation()">
                    Create
                </span>
                <span v-else class="edit-button" @click="updateOperation()">
                    Save
                </span>
            </span>
        </el-dialog>
        <el-dialog :show-close="false" :visible.sync="dialogStatusOperation" width="18%">
            <div style="padding:30px 20px 0 20px;">
                <el-row>
                    <p>*Ban status</p>
                    <el-switch v-model="data.isLogin" active-text="Banned" inactive-text="Normal">
                    </el-switch>
                </el-row>
                <el-row style="margin: 20px 0;">
                    <p>*Mute status</p>
                    <el-switch v-model="data.isWord" active-text="Muted" inactive-text="Normal">
                    </el-switch>
                </el-row>
                <el-row style="margin: 20px 0;">
                    <p>*Set as admin</p>
                    <el-switch v-model="isAdmin" active-text="Admin" inactive-text="User">
                    </el-switch>
                </el-row>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="channel-button" @click="cannel()">
                    Cancel
                </span>
                <span class="edit-button" @click="comfirmStatus()">
                    Apply
                </span>
            </span>
        </el-dialog>
    </el-row>
</template>

<script>
export default {
    data() {
        return {
            userPwd: '',
            userAvatar: '',
            data: {},
            filterText: '',
            isAdmin: false, // 是否是管理员标志，初始值为false
            currentPage: 1,
            pageSize: 10,
            totalItems: 0,
            dialogStatusOperation: false,
            dialogUserOperaion: false, // 开关
            isOperation: false, // 开关-标识新增或修改
            tableData: [],
            searchTime: [],
            selectedRows: [],
            status: null,
            userQueryDto: {}, // 搜索条件
            loginStatuList: [{ value: null, label: 'All' }, { value: 0, label: 'Normal' }, { value: 1, label: 'Banned' }],
            wordStatuList: [{ value: null, label: 'All' }, { value: 0, label: 'Normal' }, { value: 1, label: 'Muted' }],
            rolesList: [{ value: null, label: 'All' }, { value: 2, label: 'User' }, { value: 1, label: 'Admin' }]
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        cannel() {
            this.data = {};
            this.userAvatar = '';
            this.userPwd = '';
            this.dialogUserOperaion = false;
            this.dialogStatusOperation = false;
            this.isOperation = false;
        },
        comfirmStatus() {
            const userUpdateDto = {
                id: this.data.id,
                isLogin: this.data.isLogin,
                userRole: this.isAdmin ? 1 : 2,
                isWord: this.data.isWord
            }
            this.$axios.put(`/user/backUpdate`, userUpdateDto).then(res => {
                if (res.data.code === 200) {
                    this.$notify({
                        duration: 1500,
                        title: 'Updated',
                        message: 'Updated successfully',
                        type: 'success'
                    });
                    this.cannel();
                    this.fetchFreshData();
                }
            }).catch(error => {
                console.log("Status update error：" + error);
            })
        },
        // 修改用户状态
        handleStatus(data) {
            // 设置用户角色
            this.isAdmin = data.userRole === 1;
            this.dialogStatusOperation = true;
            this.data = data;
        },
        // 头像上传回调函数
        handleAvatarSuccess(res, file) {
            this.$notify({
                duration: 1500,
                title: 'Avatar upload',
                message: res.code === 200 ? 'Uploaded' : 'Upload failed',
                type: res.code === 200 ? 'success' : 'error'
            });
            // 上传成功则更新用户头像
            if (res.code === 200) {
                this.userAvatar = res.data;
            }
        },
        // 批量删除数据
        async batchDelete() {
            if (!this.selectedRows.length) {
                this.$message(`No data selected`);
                return;
            }
            const confirmed = await this.$swalConfirm({
                title: 'Delete users',
                text: `This action cannot be undone. Continue?`,
                icon: 'warning',
            });
            if (confirmed) {
                try {
                    let ids = this.selectedRows.map(entity => entity.id);
                    const response = await this.$axios.post(`/user/batchDelete`, ids);
                    if (response.data.code === 200) {
                        this.$notify({
                            duration: 1000,
                            title: 'Delete',
                            message: 'Deleted successfully',
                            type: 'success'
                        });
                        this.fetchFreshData();
                        return;
                    }
                } catch (error) {
                    this.$message.error("User delete error:", error);
                    console.error(`User delete error:`, error);
                }
            }
        },
        // 修改信息
        async updateOperation() {
            if (this.userPwd !== '') {
                const pwd = this.$md5(this.$md5(this.userPwd));
                this.data.userPwd = pwd;
            } else {
                this.data.userPwd = null;
            }
            this.data.userAvatar = this.userAvatar;
            try {
                const response = await this.$axios.put('/user/backUpdate', this.data);
                if (response.data.code === 200) {
                    this.$notify({
                        duration: 1000,
                        title: 'Updated',
                        message: 'Updated successfully',
                        type: 'success'
                    });
                    this.cannel();
                    this.fetchFreshData();
                }
            } catch (error) {
                console.error('Update error:', error);
            }
        },
        // 信息新增
        async addOperation() {
            //  密码处理
            if (this.userPwd !== '') {
                this.data.userPwd = this.$md5(this.$md5(this.userPwd));
            } else {
                this.data.userPwd = null;
            }
            this.data.userAvatar = this.userAvatar;
            try {
                const response = await this.$axios.post('/user/insert', this.data);
                if (response.data.code === 200) {
                    this.$notify({
                        duration: 1000,
                        title: 'Created',
                        message: 'Created successfully',
                        type: 'success'
                    });
                    this.cannel();
                    this.fetchFreshData();
                }else{
                    this.$notify({
                        duration: 1000,
                        title: 'Created',
                        message: response.data.msg,
                        type: 'info'
                    });
                }
            } catch (error) {
                console.error('Create error:', error);
                this.$message.error('Submit failed, please try again later!');
            }
        },
        async fetchFreshData() {
            try {
                this.tableData = [];
                let startTime = null;
                let endTime = null;
                if (this.searchTime != null && this.searchTime.length === 2) {
                    const [startDate, endDate] = await Promise.all(this.searchTime.map(date => date.toISOString()));
                    startTime = `${startDate.split('T')[0]}T00:00:00`;
                    endTime = `${endDate.split('T')[0]}T23:59:59`;
                }
                // 请求参数
                const params = {
                    current: this.currentPage,
                    size: this.pageSize,
                    key: this.filterText,
                    startTime: startTime,
                    endTime: endTime,
                    ...this.userQueryDto
                };
                const response = await this.$axios.post('/user/query', params);
                const { data } = response;
                this.tableData = data.data;
                this.totalItems = data.total;
            } catch (error) {
                this.$message.error("User query error:", error);
                console.error('User query error:', error);
            }
        },
        add() {
            this.dialogUserOperaion = true;
        },
        handleFilter() {
            this.currentPage = 1;
            this.fetchFreshData();
        },
        handleFilterClear() {
            this.filterText = '';
            this.handleFilter();
        },
        handleSizeChange(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.fetchFreshData();
        },
        handleCurrentChange(val) {
            this.currentPage = val;
            this.fetchFreshData();
        },
        handleEdit(row) {
            this.dialogUserOperaion = true;
            this.isOperation = true;
            row.userPwd = null;
            this.userAvatar = row.userAvatar;
            this.data = { ...row }
        },
        handleDelete(row) {
            this.selectedRows.push(row);
            this.batchDelete();
        }
    },
};
</script>
<style scoped lang="scss"></style>