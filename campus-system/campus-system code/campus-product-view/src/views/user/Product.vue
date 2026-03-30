<template>
    <div>
        <div class="nav-category">
            <div class="left">
                <span :style="{
                    color: categorySelectedItem.name === isUseCategory.name ? 'rgb(248,248,248)' : '',
                    backgroundColor: categorySelectedItem.name === isUseCategory.name ? 'rgb(255,209,80)' : ''
                }" @click="categorySelected(isUseCategory)" :key="index"
                    v-for="(isUseCategory, index) in isUseCategoryList">
                    {{ isUseCategory.name }}
                </span>
            </div>
            <div class="right">
                <span class="bargain">
                    <span :style="{
                        color: bargainSelectedItem.name === bargain.name ? 'rgb(248,248,248)' : '',
                        backgroundColor: bargainSelectedItem.name === bargain.name ? 'rgb(255, 209, 80)' : ''
                    }" @click="bargainSelected(bargain)" v-for="(bargain, index) in bargainStatus" :key="index">{{
                        bargain.name }}</span>
                </span>
                <el-date-picker style="width: 216px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="searchTime" type="daterange" range-separator="to" start-placeholder="Start Date"
                    end-placeholder="End Date">
                </el-date-picker>
                <el-select style="width: 100px;margin-right: 5px;" @change="fetchFreshData" size="small"
                    v-model="productQueryDto.categoryId" placeholder="Category">
                    <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id">
                    </el-option>
                </el-select>
            </div>
        </div>
        <div class="product-list">
            <el-row v-if="productList.length === 0">
                <el-empty description="No product information available"></el-empty>
            </el-row>
            <el-row v-else>
                <el-col @click.native="route(product)" :span="6" v-for="(product, index) in productList" :key="index">
                    <div class="item-product">
                        <div class="cover">
                            <img :src="coverListParse(product)" alt="" srcset="">
                        </div>
                        <div style="display: flex;justify-content: left;gap: 4px;align-items: center;">
                            <span class="bargain-hover">{{ product.isBargain ? 'Negotiable' : 'Non-negotiable' }}</span>
                            <span class="title">
                                {{ product.name }}
                            </span>
                        </div>
                        <div style="padding-block: 15px;">
                            <span class="decimel-symbol">¥</span>
                            <span class="price">{{ product.price }}</span>
                            <span class="love">4 people want this</span>
                        </div>
                        <div class="info">
                            <img :src="product.userAvatar" alt="" srcset="">
                            <span>{{ product.userName }}</span>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
    </div>
</template>
<script>
export default {
    name: 'Product',
    data() {
        return {
            categoryList: [], // Stored product category array
            isUseCategoryList: [], // Stored enabled category array
            categorySelectedItem: {},
            productQueryDto: {}, // Product query criteria object
            productList: [],// Stores the product data list returned by the backend
            bargainSelectedItem: {},
            searchTime: [],
            bargainStatus: [{ isBargain: null, name: 'All' }, { isBargain: true, name: 'Negotiable' }, { isBargain: false, name: 'Non-negotiable' }]

        };
    },
    created() {
        this.fetchFreshData();
        this.fetchCategoryList();
        // When the page loads, bargaining filter is disabled by default
        this.bargainSelected(this.bargainStatus[0]);
    },
    methods: {
        route(product) {
            // Go to product detail page
            this.$router.push('/product-detail?productId=' + product.id);
        },
        coverListParse(product) {
            if (product.coverList === null) {
                return;
            }
            const newCoverList = product.coverList.split(',');
            return newCoverList[0];
        },
        /**
         * Bargain option selected event
         * @param {*} bargain 
         */
        bargainSelected(bargain) {
            this.bargainSelectedItem = bargain;
            this.productQueryDto.isBargain = bargain.isBargain;
            this.fetchFreshData();
        },
        /**
         * Query product data
         */
        async fetchFreshData() {
            let startTime = null;
            let endTime = null;
            if (this.searchTime != null && this.searchTime.length === 2) {
                const [startDate, endDate] = await Promise.all(this.searchTime.map(date => date.toISOString()));
                startTime = `${startDate.split('T')[0]}T00:00:00`;
                endTime = `${endDate.split('T')[0]}T23:59:59`;
            }
            // this.productQueryDto.current = this.currentPage;
            // this.productQueryDto.size = this.pageSize;
            this.productQueryDto.startTime = startTime;
            this.productQueryDto.endTime = endTime;
            this.$axios.post('/product/query', this.productQueryDto).then(res => {
                const { data } = res; // Destructuring
                if (data.code === 200) {
                    this.productList = data.data;
                }
            }).catch(error => {
                console.log("Product query error:", error);
            })
        },
        /**
         * Product category selected event
         * @param {*} category 
         */
        categorySelected(category) {
            this.categorySelectedItem = category;
            this.productQueryDto.categoryId = category.id;
            // Query product data under the selected category
            this.fetchFreshData();
        },
        /**
         * Load product category data
         */
        fetchCategoryList() {
            this.$axios.post('/category/query', {}).then(res => {
                const { data } = res; // Destructuring
                if (data.code === 200) {
                    this.categoryList = data.data;
                    this.isUseCategoryList = data.data.filter(category => category.isUse);
                    this.isUseCategoryList.unshift({ id: null, name: 'All' });
                    this.categorySelected(this.isUseCategoryList[0]);
                }
            }).catch(error => {
                console.log("Product category query error:", error);
            })
        },
    }
};
</script>
<style scoped lang="scss">
.cover {
    img {
        width: 100%;
        height: 240px;
        border-radius: 10px;
    }
}

.bargain-hover {
    font-size: 12px;
    font-weight: 800;
    background-color: rgb(255, 230, 15);
    color: rgb(51, 51, 51);
    border-radius: 2px;
    padding: 2px 6px;
}

.title {
    font-size: 20px;
    color: #1f1f1f;
}

.decimel-symbol {
    font-size: 14px;
    color: #ff4f24;
    font-weight: 800;
}

.price {
    font-size: 24px;
    color: #ff4f24;
    font-weight: 800;
    margin-right: 6px;
}

.love {
    font-size: 14px;
    color: #999;
}

.info {
    display: flex;
    justify-content: left;
    align-items: center;
    gap: 4px;

    img {
        width: 20px;
        height: 20px;
        border-radius: 50%;
    }

    span {
        font-size: 14px;
        color: #999;
    }
}

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

.product-list {
    padding-block: 20px;

    .item-product {
        padding: 10px 10px 16px 10px;
        box-sizing: border-box;
        border-radius: 15px;
        transition: all .5s;
        cursor: pointer;
    }

    .item-product:hover {
        box-shadow: 1px 4px 6px rgba(0, 0, 0, 0.1), 0 1px 3px rgba(0, 0, 0, 0.06);
    }
}

.nav-category {
    display: flex;
    justify-content: space-between;

    .left {
        display: flex;
        justify-content: left;
        gap: 6px;

        span {
            display: inline-block;
            background-color: rgb(246, 246, 246);
            padding: 6px 22px;
            cursor: pointer;
            border-radius: 15px;
        }

        span:hover {
            background-color: rgb(242, 242, 242);
        }
    }
}
</style>