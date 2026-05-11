import Vue from "vue";
import VueRouter from "vue-router";
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import { getToken } from "@/utils/storage.js";
import echarts from 'echarts';
Vue.prototype.$echarts = echarts;
Vue.use(ElementUI);
Vue.use(VueRouter);

const routes = [
  { path: "/", component: () => import(`@/views/user/Home.vue`) },
  { path: "/login", component: () => import(`@/views/login/Login.vue`) },
  { path: "/register", component: () => import(`@/views/register/Register.vue`) },
  {
    path: "/admin",
    component: () => import(`@/views/admin/Home.vue`),
    meta: { requireAuth: true },
    children: [
      { path: "/adminLayout", name: 'Dashboard', icon: 'el-icon-pie-chart', component: () => import(`@/views/admin/Main.vue`), meta: { requireAuth: true } },
      { path: "/userManage", name: 'User Management', icon: 'el-icon-user-solid', component: () => import(`@/views/admin/User.vue`), meta: { requireAuth: true } },
      { path: "/productManage", name: 'Product Management', icon: 'el-icon-shopping-bag-1', component: () => import(`@/views/admin/Product.vue`), meta: { requireAuth: true } },
      { path: "/ordersManage", name: 'Order Management', icon: 'el-icon-document-copy', component: () => import(`@/views/admin/Orders.vue`), meta: { requireAuth: true } },
      { path: "/categoryManage", name: 'Product Category Management', icon: 'el-icon-document-checked', component: () => import(`@/views/admin/Category.vue`), meta: { requireAuth: true } },
      { path: "/messageManage", name: 'Message Management', icon: 'el-icon-chat-square', component: () => import(`@/views/admin/Message.vue`), meta: { requireAuth: true } },
      { path: "/interactionManage", name: 'Interaction Management', icon: 'el-icon-connection', component: () => import(`@/views/admin/Interaction.vue`), meta: { requireAuth: true } },
      { path: "/operationLogManage", name: 'Log Management', icon: 'el-icon-connection', component: () => import(`@/views/admin/OperationLog.vue`), meta: { requireAuth: true } },
      { path: "/evaluations", name: 'Comment Management', icon: 'el-icon-chat-dot-round', component: () => import(`@/views/admin/Evaluations.vue`), meta: { requireAuth: true } },
      { name: 'Admin Product Details', show: false, path: "/product-detail1", component: () => import(`@/views/admin/ProductDetail.vue`), meta: { requireAuth: true } },
    ]
  },
  {
    path: "/user",
    component: () => import(`@/views/user/Home.vue`),
    meta: { requireAuth: true },
    children: [
      { name: 'Products', path: "/product", component: () => import(`@/views/user/Product.vue`), meta: { requireAuth: true } },
      { name: 'My Products', path: "/myProduct", component: () => import(`@/views/user/MyProduct.vue`), meta: { requireAuth: true } },
      { name: 'My Favorites', path: "/mySave", component: () => import(`@/views/user/MySave.vue`), meta: { requireAuth: true } },
      { name: 'Browsing History', path: "/myView", component: () => import(`@/views/user/MyView.vue`), meta: { requireAuth: true } },
      { name: 'Orders', path: "/orders", component: () => import(`@/views/user/Orders.vue`), meta: { requireAuth: true } },
      { name: 'Messages', path: "/message", component: () => import(`@/views/user/Message.vue`), meta: { requireAuth: true } },
      { name: 'Profile', path: "/myself", component: () => import(`@/views/user/Myself.vue`), meta: { requireAuth: true } },
      { name: 'Search', path: "/search", show: false, auth: false, component: () => import(`@/views/user/Search.vue`), meta: { requireAuth: true } },
      { name: 'Post Product', path: "/post-product", component: () => import(`@/views/user/PostProduct.vue`), meta: { requireAuth: true } },
      { name: 'Edit Product', path: "/edit-product", component: () => import(`@/views/user/EditProduct.vue`), meta: { requireAuth: true } },
      { name: 'Product Details', path: "/product-detail", component: () => import(`@/views/user/ProductDetail.vue`), meta: { requireAuth: true } },
    ]
  }
];

const router = new VueRouter({
  routes,
  mode: 'history'
});
router.beforeEach((to, from, next) => {
  if (to.meta.requireAuth) {
    const token = getToken();
    // 需要放行的路由
    const releasePaths = ['/main', '/product'];
    if (releasePaths.includes(to.path)) {
      next();
    } else {
      if (token !== null) {
        next();
      } else {
        next("/login");
      }
    }
  }
  else {
    next();
  }
});

export default router;