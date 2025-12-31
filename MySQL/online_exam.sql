/*
 Navicat Premium Dump SQL

 Source Server         : mySQL8043
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : online_exam

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 31/12/2025 09:58:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admission_scoreline
-- ----------------------------
DROP TABLE IF EXISTS `admission_scoreline`;
CREATE TABLE `admission_scoreline`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `exam_year` year NOT NULL,
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `min_score` decimal(5, 2) NOT NULL,
  `set_by` int NOT NULL,
  `set_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `set_by`(`set_by` ASC) USING BTREE,
  CONSTRAINT `admission_scoreline_ibfk_1` FOREIGN KEY (`set_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admission_scoreline
-- ----------------------------
INSERT INTO `admission_scoreline` VALUES (1, 2024, '计算机科学与技术', 85.00, 5, '2025-12-29 10:38:19');
INSERT INTO `admission_scoreline` VALUES (2, 2024, '软件工程', 80.00, 5, '2025-12-29 10:38:19');
INSERT INTO `admission_scoreline` VALUES (3, 2024, '人工智能', 82.00, 5, '2025-12-29 10:38:19');

-- ----------------------------
-- Table structure for applications
-- ----------------------------
DROP TABLE IF EXISTS `applications`;
CREATE TABLE `applications`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `exam_year` year NOT NULL,
  `exam_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `application_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('待确认','已确认','已取消') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '待确认',
  `confirmation_time` timestamp NULL DEFAULT NULL,
  `confirmed_by` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `student_id`(`student_id` ASC) USING BTREE,
  INDEX `confirmed_by`(`confirmed_by` ASC) USING BTREE,
  CONSTRAINT `applications_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `applications_ibfk_2` FOREIGN KEY (`confirmed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of applications
-- ----------------------------
INSERT INTO `applications` VALUES (1, 1, 2024, '自主招生', '2025-12-29 10:38:09', '已确认', NULL, 5);
INSERT INTO `applications` VALUES (2, 2, 2024, '自主招生', '2025-12-29 10:38:09', '待确认', NULL, NULL);
INSERT INTO `applications` VALUES (3, 3, 2024, '综合评价', '2025-12-29 10:38:09', '已确认', NULL, 5);

-- ----------------------------
-- Table structure for exam_info
-- ----------------------------
DROP TABLE IF EXISTS `exam_info`;
CREATE TABLE `exam_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `publish_by` int NOT NULL,
  `publish_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_active` tinyint(1) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `publish_by`(`publish_by` ASC) USING BTREE,
  CONSTRAINT `exam_info_ibfk_1` FOREIGN KEY (`publish_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_info
-- ----------------------------
INSERT INTO `exam_info` VALUES (1, '2024年自主招生报考须知', '请考生认真阅读报考流程和注意事项。', 5, '2025-12-29 10:38:32', 1);
INSERT INTO `exam_info` VALUES (2, '考场规则说明', '考试期间请遵守考场纪律。', 5, '2025-12-29 10:38:32', 1);
INSERT INTO `exam_info` VALUES (3, '成绩查询通知', '成绩将于7月1日公布。', 5, '2025-12-29 10:38:32', 1);

-- ----------------------------
-- Table structure for exam_rooms
-- ----------------------------
DROP TABLE IF EXISTS `exam_rooms`;
CREATE TABLE `exam_rooms`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `application_id` int NOT NULL,
  `room_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `exam_date` date NULL DEFAULT NULL,
  `exam_time` time NULL DEFAULT NULL,
  `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `assigned_by` int NOT NULL,
  `assigned_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `application_id`(`application_id` ASC) USING BTREE,
  INDEX `assigned_by`(`assigned_by` ASC) USING BTREE,
  CONSTRAINT `exam_rooms_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `exam_rooms_ibfk_2` FOREIGN KEY (`assigned_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_rooms
-- ----------------------------
INSERT INTO `exam_rooms` VALUES (1, 1, 'A101', '01', '2024-06-07', '09:00:00', '教学楼A栋101室', 5, '2025-12-29 10:38:25');
INSERT INTO `exam_rooms` VALUES (2, 2, 'A101', '02', '2024-06-07', '09:00:00', '教学楼A栋101室', 5, '2025-12-29 10:38:25');
INSERT INTO `exam_rooms` VALUES (3, 3, 'B202', '05', '2024-06-08', '14:00:00', '教学楼B栋202室', 5, '2025-12-29 10:38:25');

-- ----------------------------
-- Table structure for login_history
-- ----------------------------
DROP TABLE IF EXISTS `login_history`;
CREATE TABLE `login_history`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `login_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `login_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of login_history
-- ----------------------------
INSERT INTO `login_history` VALUES (1, 1, '2025-12-29 10:38:04', '192.168.1.100', 'Chrome/Windows');
INSERT INTO `login_history` VALUES (2, 1, '2025-12-29 10:38:04', '192.168.1.101', 'Firefox/Windows');
INSERT INTO `login_history` VALUES (3, 2, '2025-12-29 10:38:04', '192.168.1.102', 'Safari/Mac');
INSERT INTO `login_history` VALUES (4, 4, '2025-12-29 10:38:04', '10.0.0.1', 'Chrome/Linux');
INSERT INTO `login_history` VALUES (5, 5, '2025-12-29 10:38:04', '10.0.0.2', 'Edge/Windows');

-- ----------------------------
-- Table structure for scores
-- ----------------------------
DROP TABLE IF EXISTS `scores`;
CREATE TABLE `scores`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `application_id` int NOT NULL,
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `score` decimal(5, 2) NULL DEFAULT NULL,
  `entry_by` int NOT NULL,
  `entry_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `application_id`(`application_id` ASC) USING BTREE,
  INDEX `entry_by`(`entry_by` ASC) USING BTREE,
  CONSTRAINT `scores_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `scores_ibfk_2` FOREIGN KEY (`entry_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scores
-- ----------------------------
INSERT INTO `scores` VALUES (1, 1, '数学', 95.50, 5, '2025-12-29 10:38:14');
INSERT INTO `scores` VALUES (2, 1, '英语', 88.00, 5, '2025-12-29 10:38:14');
INSERT INTO `scores` VALUES (3, 2, '数学', 92.00, 5, '2025-12-29 10:38:14');
INSERT INTO `scores` VALUES (4, 3, '数学', 85.50, 5, '2025-12-29 10:38:14');
INSERT INTO `scores` VALUES (5, 3, '英语', 90.00, 5, '2025-12-29 10:38:14');
INSERT INTO `scores` VALUES (6, 3, '物理', 78.50, 5, '2025-12-29 10:38:14');

-- ----------------------------
-- Table structure for students
-- ----------------------------
DROP TABLE IF EXISTS `students`;
CREATE TABLE `students`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gender` enum('男','女') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `birth_date` date NULL DEFAULT NULL,
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `students_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of students
-- ----------------------------
INSERT INTO `students` VALUES (1, 1, '110101200001011234', '男', '2000-01-01', '北京一中', '计算机科学与技术', '北京市海淀区');
INSERT INTO `students` VALUES (2, 2, '110101200002022345', '女', '2000-02-02', '上海二中', '软件工程', '上海市浦东新区');
INSERT INTO `students` VALUES (3, 3, '110101200003033456', '男', '2000-03-03', '广州三中', '人工智能', '广州市天河区');

-- ----------------------------
-- Table structure for system_logs
-- ----------------------------
DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE `system_logs`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `log_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `system_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_logs
-- ----------------------------
INSERT INTO `system_logs` VALUES (1, 4, '用户管理模块访问', '2025-12-29 10:38:37', '10.0.0.1');
INSERT INTO `system_logs` VALUES (2, 4, '数据库备份操作', '2025-12-29 10:38:37', '10.0.0.1');
INSERT INTO `system_logs` VALUES (3, 5, '分数线设置操作', '2025-12-29 10:38:37', '10.0.0.2');
INSERT INTO `system_logs` VALUES (4, 5, '考场分配操作', '2025-12-29 10:38:37', '10.0.0.2');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('student','system_admin','recruitment_admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `ux_users_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `ux_users_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'zhangsan', '123456', 'student', '张三', '13800138001', 'zhangsan@example.com', '2025-12-29 10:37:52', '2025-12-29 10:37:52');
INSERT INTO `users` VALUES (2, 'lisi', '123456', 'student', '李四', '13800138002', 'lisi@example.com', '2025-12-29 10:37:52', '2025-12-29 10:37:52');
INSERT INTO `users` VALUES (3, 'wangwu', '123456', 'student', '王五', '13800138003', 'wangwu@example.com', '2025-12-29 10:37:52', '2025-12-29 10:37:52');
INSERT INTO `users` VALUES (4, 'admin', '123456', 'system_admin', '系统管理员', '12800138005', 'admin@edu.com', '2025-12-29 10:37:52', '2025-12-31 09:58:11');
INSERT INTO `users` VALUES (5, 'zhaoliu', '123456', 'recruitment_admin', '赵六', '13800138004', 'zhaoliu@edu.com', '2025-12-29 10:37:52', '2025-12-29 10:37:52');
INSERT INTO `users` VALUES (6, 'testphone_dup', '123456', 'student', '重复手机号测试', '12345678912', 'new123@qq.com', '2025-12-31 09:39:17', '2025-12-31 09:39:17');
INSERT INTO `users` VALUES (10, 'test_phone', '123456', 'student', NULL, '12345678913', '123@qq.com', '2025-12-31 09:45:11', '2025-12-31 09:45:11');
INSERT INTO `users` VALUES (11, 'test_phone1', '123456', 'student', NULL, '12345678914', '1234@qq.com', '2025-12-31 09:46:01', '2025-12-31 09:46:01');

SET FOREIGN_KEY_CHECKS = 1;
