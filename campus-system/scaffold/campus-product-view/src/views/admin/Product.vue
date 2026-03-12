<template>
    <el-row style="background-color: #FFFFFF;padding: 5px 0;border-radius: 5px;">
        <el-row style="padding: 10px;margin-left: 5px;">
            <el-row>
                <span class="bargain">
                    <span
                        :style="{ backgroundColor: bargainSelectedItem.name === bargain.name ? 'rgb(255,255,255)' : '' }"
                        @click="bargainSelected(bargain)" v-for="(bargain, index) in bargainStatus" :key="index">{{
                            bargain.name }}</span>
                </span>
                <el-select style="width: 100px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="productQueryDto.categoryId" placeholder="Category">
                    <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id">
                    </el-option>
                </el-select>
                <el-date-picker style="width: 216px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="searchTime" type="daterange" range-separator="to" start-placeholder="Start Date"
                    end-placeholder="End Date">
                </el-date-picker>
                <el-input size="small" style="width: 166px;" v-model="productQueryDto.name" placeholder="Product Name" clearable
                    @clear="handleFilterClear">
                    <el-button slot="append" @click="handleFilter" icon="el-icon-search"></el-button>
                </el-input>
            </el-row>
        </el-row>
        <el-row style="margin: 0 22px;border-top: 1px solid rgb(245,245,245);">
            <el-table :stripe="true" :data="tableData">
                <el-table-column prop="userAvatar" width="68" label="Avatar">
                    <template slot-scope="scope">
                        <el-avatar :size="25" :src="scope.row.userAvatar" style="margin-top: 10px;"></el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="userName" width="110" label="User"></el-table-column>
                <el-table-column prop="name" label="Product Name"></el-table-column>
                <el-table-column prop="categoryName" width="110" label="Category"></el-table-column>
                <el-table-column prop="price" width="130" label="Price">
                    <template slot-scope="scope">
                        <span>${{ scope.row.price }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="inventory" width="110" label="Stock"></el-table-column>
                <el-table-column prop="oldLevel" width="168" label="Condition">
                    <template slot-scope="scope">
                        <span>{{ scope.row.oldLevel }}/10</span>
                    </template>
                </el-table-column>
                <el-table-column prop="isBargain" width="138" label="Negotiable">
                    <template slot-scope="scope">
                        <span>{{ scope.row.isBargain ? 'Negotiable' : 'Not Negotiable' }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" width="168" label="Posted Time"></el-table-column>
                <el-table-column label="Actions" width="220">
                    <template slot-scope="scope">
                        <span class="text-button" @click="handleEdit(scope.row)">Product Details</span>
                        <span class="text-button" @click="handleDelete(scope.row)">Delete</span>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination style="margin:10px 0;float: right;" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20]"
                :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
                :total="totalItems"></el-pagination>
        </el-row>
        <el-drawer title="Product Details" :visible.sync="drawerProductOperaion" :direction="direction">
            <div style="padding: 0 15px;">
                <div>
                    <span class="dialog-hover">Product Cover Images</span>
                    <div class="detail-cover">
                        <div @click="coverExpansion(cover)" class="cover" v-for="(cover, index) in coverList"
                            :key="index">
                            <img :src="cover">
                        </div>
                    </div>
                </div>
                <div>
                    <span class="dialog-hover">Product Details</span>
                    <div style="padding: 10px 6px;" v-html="data.detail"></div>
                </div>
            </div>
        </el-drawer>
        <el-dialog :visible.sync="dialogCoverExpansion" width="55%">
            <div style="padding: 80px;display: flex;justify-content: center;align-items: center;">
                <img style="width: 500px;height: 500px;" :src="cover" alt="" srcset="">
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
            cover: null, // Enlarged product image
            pageSize: 10,
            totalItems: 0,
            drawerProductOperaion: false, // Switch
            isOperation: false, // Switch - marks add or edit
            tableData: [],
            delectedRows: [],
            dialogCoverExpansion: false,
            productQueryDto: {}, // Search conditions
            direction: 'rtl', // right to left
            categoryList: [],
            bargainSelectedItem: {},
            bargainStatus: [
                { isBargain: null, name: 'All' },
                { isBargain: true, name: 'Negotiable' },
                { isBargain: false, name: 'Not Negotiable' }
            ]
        };
    },
    created() {
        this.fetchCategoryList();
        this.fetchFreshData();
        // By default, do not enable the bargaining filter when the page loads
        this.bargainSelected(this.bargainStatus[0]);
    },
    methods: {
        /**
         * Bargain status selected event
         * @param {*} bargain Bargain status
         */
        bargainSelected(bargain) {
            this.bargainSelectedItem = bargain;
            this.productQueryDto.isBargain = bargain.isBargain;
            this.fetchFreshData();
        },
        /**
         * Load product category data
         */
        fetchCategoryList() {
            this.$axios.post('/category/query', {}).then(res => {
                const { data } = res;
                if (data.code === 200) {
                    this.categoryList = data.data;
                    this.categoryList.unshift({ id: null, name: 'All' });
                }
            }).catch(error => {
                console.log("Category query error:", error);
            })
        },
        /**
         * Enlarge product image
         * @param {*} cover Image URL
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
        // Batch delete data
        async batchDelete() {
            if (!this.delectedRows.length) {
                this.$message(`No data selected`);
                return;
            }
            const confirmed = await this.$swalConfirm({
                title: 'Delete Product Data',
                text: `This action cannot be undone. Do you want to continue?`,
                icon: 'warning',
            });
            if (confirmed) {
                try {
                    let ids = this.delectedRows.map(entity => entity.id);
                    const response = await this.$axios.post(`/product/batchDelete`, ids);
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
                    this.$message.error("Product deletion error:", error);
                    console.error(`Product deletion error:`, error);
                }
            }
        },
        /**
         * Product query
         */
        async fetchFreshData() {
            let startTime = null;
            let endTime = null;
            if (this.searchTime != null && this.searchTime.length === 2) {
                const [startDate, endDate] = await Promise.all(this.searchTime.map(date => date.toISOString()));
                startTime = `${startDate.split('T')[0]}T00:00:00`;
                endTime = `${endDate.split('T')[0]}T23:59:59`;
            }
            this.productQueryDto.current = this.currentPage;
            this.productQueryDto.size = this.pageSize;
            this.productQueryDto.startTime = startTime;
            this.productQueryDto.endTime = endTime;
            this.$axios.post('/product/query', this.productQueryDto).then(res => {
                const { data } = res;
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
            this.parseCoverList(row.coverList);
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