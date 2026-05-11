<template>
    <el-row style="background-color: #FFFFFF;padding: 5px 0;border-radius: 5px;">
        <el-row style="padding: 10px;margin-left: 5px;">
            <el-row>
                <el-input size="small" style="width: 166px;" v-model="operationLogQueryDto.detail" placeholder="Action"
                    clearable @clear="handleFilterClear">
                    <el-button slot="append" @click="handleFilter" icon="el-icon-search"></el-button>
                </el-input>
            </el-row>
        </el-row>
        <el-row style="margin: 0 22px;border-top: 1px solid rgb(245,245,245);">
            <el-table :stripe="true" :data="tableData">
                <el-table-column prop="userAccount" width="168" label="User Account"></el-table-column>
                <el-table-column prop="userName" width="228" label="Username"></el-table-column>
                <el-table-column prop="detail" label="Operation"></el-table-column>
                <el-table-column prop="createTime" width="208" :sortable="true" label="Operation Time"></el-table-column>
                <el-table-column label="Actions" width="120">
                    <template slot-scope="scope">
                        <span class="text-button" @click="handleDelete(scope.row)">Delete</span>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination style="margin:10px 0;float: right;" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20]"
                :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
                :total="totalItems"></el-pagination>
        </el-row>
    </el-row>
</template>

<script>
export default {
    data() {
        return {
            data: {},
            currentPage: 1,
            pageSize: 10,
            totalItems: 0,
            tableData: [],
            delectedRows: [],
            operationLogQueryDto: {}, // 搜索条件
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
                title: 'Delete Operation Log Data',
                text: `This cannot be restored after deletion. Continue?`,
                icon: 'warning',
            });
            if (confirmed) {
                try {
                    let ids = this.delectedRows.map(entity => entity.id);
                    const response = await this.$axios.post(`/operation-log/batchDelete`, ids);
                    if (response.data.code === 200) {
                        this.$notify({
                            duration: 1000,
                            title: 'Delete Information',
                            message: 'Deleted successfully',
                            type: 'success'
                        });
                        this.fetchFreshData();
                        return;
                    }
                } catch (error) {
                    this.$message.error("Failed to delete operation log information: ", error);
                    console.error(`Failed to delete operation log information: `, error);
                }
            }
        },
        /**
         * 操作日志查询
         */
        fetchFreshData() {
            this.operationLogQueryDto.current = this.currentPage;
            this.operationLogQueryDto.size = this.pageSize;
            this.$axios.post('/operation-log/query', this.operationLogQueryDto).then(res => {
                const { data } = res; // 解构
                if (data.code === 200) {
                    this.tableData = data.data;
                    this.totalItems = data.total;
                }
            }).catch(error => {
                this.$notify.error({
                    title: 'Query Operation',
                    message: error
                });
            })
        },
        handleFilter() {
            this.currentPage = 1;
            this.fetchFreshData();
        },
        handleFilterClear() {
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
        handleDelete(row) {
            this.delectedRows.push(row);
            this.batchDelete();
        }
    },
};
</script>
<style scoped lang="scss"></style>