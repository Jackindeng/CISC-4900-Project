<template>
    <el-row style="background-color: #FFFFFF;padding: 5px 0;border-radius: 5px;">
        <el-row style="padding: 10px;margin-left: 5px;">
            <el-row>
                <span class="bargain">
                    <span
                        :style="{ backgroundColor: tradeSelectedItem.name === bargain.name ? 'rgb(255,255,255)' : '' }"
                        @click="tradeSelected(bargain)" v-for="(bargain, index) in tradeStatus" :key="index">{{
                            bargain.name }}</span>
                </span>
                <span class="bargain">
                    <span
                        :style="{ backgroundColor: refundSelectedItem.name === bargain.name ? 'rgb(255,255,255)' : '' }"
                        @click="refundSelected(bargain)" v-for="(bargain, index) in refundStatus" :key="index">{{
                            bargain.name }}</span>
                </span>
                <el-date-picker style="width: 216px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="searchTime" type="daterange" range-separator="to" start-placeholder="Order Start"
                    end-placeholder="Order End">
                </el-date-picker>
                <el-input size="small" style="width: 166px;" v-model="ordersQueryDto.code" placeholder="Order No." clearable
                    @clear="handleFilterClear">
                    <el-button slot="append" @click="handleFilter" icon="el-icon-search"></el-button>
                </el-input>
            </el-row>
        </el-row>
        <el-row style="margin: 0 22px;border-top: 1px solid rgb(245,245,245);">
            <el-table :stripe="true" :data="tableData">
                <el-table-column prop="code" width="140" label="Order No."></el-table-column>
                <el-table-column prop="userAvatar" width="68" label="Avatar">
                    <template slot-scope="scope">
                        <el-avatar :size="25" :src="scope.row.userAvatar" style="margin-top: 10px;"></el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="userName" width="130" label="User"></el-table-column>
                <el-table-column prop="productTitle" width="168" label="Order Name"></el-table-column>
                <el-table-column prop="buyPrice" width="110" label="Purchase Price"></el-table-column>
                <el-table-column prop="buyNumber" width="110" label="Purchase Quantity"></el-table-column>
                <el-table-column prop="createTime" width="168" label="Order Time"></el-table-column>
                <el-table-column prop="detail" label="Order Notes"></el-table-column>
                <el-table-column label="Actions" width="160">
                    <template slot-scope="scope">
                        <span class="text-button" @click="handleEdit(scope.row)">Order Details</span>
                        <span class="text-button" @click="handleDelete(scope.row)">Delete</span>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination style="margin:10px 0;float: right;" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20]"
                :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
                :total="totalItems"></el-pagination>
        </el-row>
        <el-drawer title="Order Details" :visible.sync="drawerProductOperaion" :direction="direction">
            <div style="padding: 0 15px;">
                <div>
                    <span class="dialog-hover">Product Cover Image</span>
                    <div class="detail-cover">
                        <div @click="coverExpansion(cover)" class="cover" v-for="(cover, index) in coverList"
                            :key="index">
                            <img :src="cover">
                        </div>
                    </div>
                </div>
                <div>
                    <div>
                        <span class="dialog-hover">Payment Status</span>
                        <i v-if="!data.tradeStatus" style="margin-right: 5px;font-size: 10px;color: rgb(253, 199, 50);"
                            class="el-icon-warning"></i>
                        <i v-else style="margin-right: 5px;color: rgb(253, 199, 50);" class="el-icon-success"></i>
                        <span class="dialog-hover" v-if="!data.tradeStatus">Unpaid</span>
                        <span class="dialog-hover" v-else>Paid</span>
                        <span class="dialog-hover" v-if="data.tradeStatus">Payment Time: {{ data.tradeTime }}</span>
                    </div>
                    <div>
                        <span class="dialog-hover">Refund Status</span>
                        <i v-if="!data.refundStatus" style="margin-right: 5px;font-size: 10px;color: rgb(253, 199, 50);"
                            class="el-icon-warning"></i>
                        <i v-else style="margin-right: 5px;color: rgb(253, 199, 50);" class="el-icon-success"></i>
                        <span class="dialog-hover" v-if="!data.refundStatus">Not Applied</span>
                        <span class="dialog-hover" v-else>Applied</span>
                        <span class="dialog-hover" v-if="data.refundStatus">Refund Time: {{ data.refundTime }}</span>
                    </div>
                    <div>
                        <span class="dialog-hover">Refund Review Status</span>
                        <i v-if="!data.isRefundConfirm" style="margin-right: 5px;font-size: 10px;color: rgb(253, 199, 50);"
                            class="el-icon-warning"></i>
                        <i v-else style="margin-right: 5px;color: rgb(253, 199, 50);" class="el-icon-success"></i>
                        <span class="dialog-hover" v-if="!data.isRefundConfirm">Not Reviewed</span>
                        <span class="dialog-hover" v-else>Reviewed</span>
                    </div>
                </div>
            </div>
        </el-drawer>
        <el-dialog :visible.sync="dialogCoverExpansion" width="35%">
            <div style="padding: 10px;display: flex;justify-content: center;align-items: center;">
                <img style="width: 100%;height: 500px;" :src="cover" alt="" srcset="">
            </div>
        </el-dialog>
    </el-row>
</template>

<script>
export default {
    data() {
        return {
            data: {},
            currentPage: 1,
            coverList: [],
            searchTime: [],
            cover: null, // 放大查看的产品图略
            pageSize: 10,
            totalItems: 0,
            drawerProductOperaion: false, // 开关
            isOperation: false, // 开关-标识新增或修改
            tableData: [],
            delectedRows: [],
            dialogCoverExpansion: false,
            ordersQueryDto: {}, // 搜索条件
            direction: 'rtl', // 从右往左 right to left
            categoryList: [],
            tradeSelectedItem: {},
            refundSelectedItem: {},
            refundStatus: [{ refundStatus: null, name: 'All' }, { refundStatus: true, name: 'Refunded' }, { refundStatus: false, name: 'Not Refunded' }],
            tradeStatus: [{ tradeStatus: null, name: 'All' }, { tradeStatus: true, name: 'Ordered' }, { tradeStatus: false, name: 'Not Ordered' }]
        };
    },
    created() {
        this.fetchFreshData();
        // 页面加载时，默认不启用砍价查询条件
        this.tradeSelected(this.tradeStatus[0]);
    },
    methods: {
        /**
         * 支付状态选中事件
         * @param {*} bargain 支付状态
         */
        tradeSelected(trade) {
            this.tradeSelectedItem = trade;
            this.ordersQueryDto.tradeStatus = trade.tradeStatus;
            this.fetchFreshData();
        },
        refundSelected(refund){
            this.refundSelectedItem = refund;
            this.ordersQueryDto.refundStatus = refund.refundStatus;
            this.fetchFreshData(); 
        },
        /**
         * 产品图放大查看
         * @param {*} cover 图片链接
         */
        coverExpansion(cover) {
            this.cover = cover;
            this.dialogCoverExpansion = true;
        },
        cannel() {
            this.data = {};
            this.drawerProductOperaion = false;
            this.isOperation = false;
        },
        // 批量删除数据
        async batchDelete() {
            if (!this.delectedRows.length) {
                this.$message(`No data selected`);
                return;
            }
            const confirmed = await this.$swalConfirm({
                title: 'Delete Order Data',
                text: `This cannot be restored after deletion. Continue?`,
                icon: 'warning',
            });
            if (confirmed) {
                try {
                    let ids = this.delectedRows.map(entity => entity.id);
                    const response = await this.$axios.post(`/orders/batchDelete`, ids);
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
                    this.$message.error("Failed to delete order information: ", error);
                    console.error(`Failed to delete order information: `, error);
                }
            }
        },
        /**
         * 订单查询
         */
        async fetchFreshData() {
            let startTime = null;
            let endTime = null;
            if (this.searchTime != null && this.searchTime.length === 2) {
                const [startDate, endDate] = await Promise.all(this.searchTime.map(date => date.toISOString()));
                startTime = `${startDate.split('T')[0]}T00:00:00`;
                endTime = `${endDate.split('T')[0]}T23:59:59`;
            }
            this.ordersQueryDto.current = this.currentPage;
            this.ordersQueryDto.size = this.pageSize;
            this.ordersQueryDto.startTime = startTime;
            this.ordersQueryDto.endTime = endTime;
            this.$axios.post('/orders/query', this.ordersQueryDto).then(res => {
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
        add() {
            this.drawerProductOperaion = true;
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
        parseCoverList(coverList) {
            this.coverList = coverList.split(',');
        },
        handleEdit(row) {
            this.data = row;
            this.parseCoverList(row.productCover);
            this.drawerProductOperaion = true;
        },
        handleDelete(row) {
            this.delectedRows.push(row);
            this.batchDelete();
        }
    },
};
</script>
<style scoped lang="scss">
.bargain {
    display: inline-block;
    font-size: 12px;
    background-color: rgb(246, 246, 246);
    line-height: 24px;
    padding-inline: 10px;
    padding-block: 4px;
    margin-right: 5px;
    border-radius: 5px;
    cursor: pointer;

    span {
        display: inline-block;
        padding-inline: 10px;
        border-radius: 5px;
    }
}

.detail-cover {
    display: flex;
    justify-content: left;
    gap: 10px;

    .cover {
        padding: 10px;
        box-sizing: border-box;
        border-radius: 5px;
        cursor: pointer;

        img {
            width: 100px;
            height: 100px;
        }
    }

    .cover:hover {
        background-color: rgb(246, 246, 246);
    }

}
</style>