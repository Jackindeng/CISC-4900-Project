<template>
    <el-row style="background-color: #FFFFFF;padding: 10px 0;border-radius: 5px;">
        <el-row style="padding: 10px;margin: 0 10px;">
            <el-row>
                <el-date-picker @change="fetchFreshData" size="small" style="width: 220px;" v-model="searchTime"
                    type="daterange" range-separator="To" start-placeholder="Start" end-placeholder="End">
                </el-date-picker>
                <el-input size="small" style="width: 188px;margin-left: 5px;margin-right: 6px;"
                    v-model="evalustionsQueryDto.content" placeholder="Comment content">
                    <el-button slot="append" @click="handleFilter" icon="el-icon-search"></el-button>
                </el-input>
            </el-row>
        </el-row>
        <el-row style="margin: 0 20px;border-top: 1px solid rgb(245,245,245);">
            <el-table :data="tableData" style="width: 100%">
                <el-table-column prop="content" label="Text">
                    <template slot-scope="scope">
                        <div class="cell-name">{{ scope.row.content }}</div>
                    </template>
                </el-table-column>
                <el-table-column prop="upvoteList" width="60" label="Likes">
                    <template slot-scope="scope">
                        <span v-if="scope.row.upvoteList !== null" style="font-size: 16px;font-weight: bolder;">{{
                            scope.row.upvoteList.split(',').length }}</span>
                        <span v-else style="font-size: 16px;font-weight: bolder;">0</span>
                    </template>
                </el-table-column>
                <el-table-column prop="contentType" width="100" label="Source">
                    <template slot-scope="scope">
                        <span>{{ scope.row.contentType }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="userName" width="140" label="Author">
                </el-table-column>
                <el-table-column prop="replierName" width="140" label="Reply to">
                </el-table-column>
                <el-table-column prop="createTime" :sortable="true" width="168" label="Time">
                </el-table-column>
                <el-table-column prop="parentId" width="120" label="Level">
                    <template slot-scope="scope">
                        <i v-if="scope.row.parentId === null" style="margin-right: 5px;" class="el-icon-warning"></i>
                        <i v-else style="margin-right: 5px;color: rgb(253, 199, 50);" class="el-icon-success"></i>
                        <span v-if="scope.row.parentId === null"
                            style="text-decoration: underline;text-decoration-style: dashed;">Parent</span>
                        <span v-else>Child</span>
                    </template>
                </el-table-column>
                <el-table-column label="Actions" width="100px" fixed="right">
                    <template slot-scope="scope">
                        <span class="text-button" @click="handleDelete(scope.row)">Delete</span>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination style="margin: 10px 0;float: right;" @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="currentPage" :page-sizes="[20, 50]" :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper" :total="totalItems"></el-pagination>
        </el-row>
    </el-row>
</template>

<script>
export default {
    data() {
        return {
            filterText: '', // 搜索关键词
            tableData: [], // 表格数据，存储查询回来的数据
            currentPage: 1, // 当前页
            pageSize: 20, // 页面大小
            totalItems: 0, // 总共的数据大小
            searchTime: [], // 搜索时间
            delectedRows: [], // 删除的数据
            evalustionsQueryDto: {}, // 搜索的条件对象
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        // 批量删除数据
        async batchDelete() {
            if (!this.delectedRows.length) {
                this.$message(`No data selected`);
                return;
            }
            const confirmed = await this.$swalConfirm({
                title: 'Delete comments',
                text: `This action cannot be undone. Continue?`,
                icon: 'warning',
            });
            if (confirmed) {
                try {
                    let ids = this.delectedRows.map(entity => entity.id);
                    const response = await this.$axios.post(`/evaluations/batchDelete`, ids);
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
                } catch (e) {
                    console.error(`Comment delete error:`, e);
                }
            }
        },
        resetQueryCondition() {
            this.filterText = '';
            this.searchTime = '';
            this.fetchFreshData();
        },
        async updateOperation() {
            try {
                const response = await this.$axios.put('/evaluations/update', this.data);
                this.$message[response.data.code === 200 ? 'success' : 'error'](response.data.msg);
                if (response.data.code === 200) {
                    this.closeDialog();
                    this.fetchFreshData();
                    this.clearFormData();
                }
            } catch (error) {
                console.error('Form submit error:', error);
                this.$message.error('Submit failed, please try again later!');
            }
        },
        async addOperation() {
            this.data.tag = this.dynamicTags.join(',');
            try {
                const response = await this.$axios.post('/evaluations/save', this.data);
                this.$message[response.data.code === 200 ? 'success' : 'error'](response.data.msg);
                if (response.data.code === 200) {
                    this.closeDialog();
                    this.fetchFreshData();
                    this.clearFormData();
                }
            } catch (error) {
                console.error('Form submit error:', error);
                this.$message.error('Submit failed, please try again later!');
            }
        },
        async fetchFreshData() {
            try {
                let startTime = '';
                let endTime = '';
                if (this.searchTime !== null && this.searchTime.length === 2) {
                    const [startDate, endDate] = await Promise.all(this.searchTime.map(date => date.toISOString()));
                    startTime = `${startDate.split('T')[0]}T00:00:00`;
                    endTime = `${endDate.split('T')[0]}T23:59:59`;
                }
                // 请求参数
                const params = {
                    current: this.currentPage,
                    size: this.pageSize,
                    startTime: startTime,
                    endTime: endTime,
                    ...this.evalustionsQueryDto,
                };
                // 使用await等待请求完成
                let response = await this.$axios.post('/evaluations/query', params);
                const { data } = response;
                this.tableData = data.data;
                this.totalItems = data.total;
            } catch (error) {
                console.error('Query error:', error);
            }
        },
        handleFilter() {
            this.currentPage = 1;
            this.fetchFreshData();
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
        async handleDelete(row) {
            this.delectedRows.push(row);
            this.batchDelete();
        }
    },
};
</script>
<style scoped lang="scss"></style>