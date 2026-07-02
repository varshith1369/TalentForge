# TalentForge — Project Guidelines

## 🎓 Final Year Project

**TalentForge**
*An AI-Powered Placement Readiness & Career Development Platform*

TalentForge is an AI-powered desktop application that provides an all-in-one platform for placement preparation. It helps students improve coding skills, aptitude, interview performance, resume quality, and overall placement readiness through interactive modules, personalized recommendations, and analytics.

**Why "TalentForge"?**

- **Talent** → Represents students' skills and abilities
- **Forge** → Means to build, strengthen, and shape those skills

---

## 🚀 Modules

1. 👤 User Authentication
2. 🏠 Dashboard
3. 💻 Coding Practice
4. 🧠 Aptitude Practice
5. 📄 Resume Checker
6. 🎤 Mock Interview
7. 🏢 Company-wise Preparation
8. 📚 Notes & Revision
9. 📅 Study Planner
10. 📈 Skill Tracker
11. 📊 Analytics Dashboard
12. 🏆 Leaderboard
13. ⚙️ Settings

---

## 🛠 Tech Stack (Fixed — no other frameworks/libraries)

- **Language:** Java
- **UI:** Java Swing
- **Database:** SQLite
- **Connectivity:** JDBC
- **Version Control:** Git & GitHub
- **IDE:** IntelliJ IDEA / Eclipse

---

## 👥 Team Members & Ownership

### 👤 Y Varshith

- UI/UX Design
- Database (SQLite)
- Login & Authentication
- Dashboard
- Analytics
- Resume Checker (UI)
- Skill Tracker (UI)

### 👤 Shabd Jain

- Coding Practice Module
- Aptitude Module
- Mock Interview Logic
- Company-wise Preparation
- Data Structures
- Recommendation Engine
- Reports

---

## 🧩 Data Structure Assignments (7 required)

| Data Structure | Used In                          | Purpose                                |
| -------------- | -------------------------------- | -------------------------------------- |
| Trie           | Coding Practice / Resume Checker | Topic autocomplete / keyword matching  |
| HashMap        | Authentication / Dashboard       | Session & user cache lookups           |
| Queue          | Aptitude Practice                | Question sequencing                    |
| PriorityQueue  | Aptitude / Leaderboard           | Adaptive difficulty / top-N rankings   |
| Stack          | Study Planner                    | Undo/redo on task edits                |
| LinkedList     | Recommendation Engine            | Ordered recommendation history         |
| Graph          | Company-wise Preparation         | Company → role → skill relationships |

---

## 🌟 Future Scope

- AI Resume Analysis
- Speech Analysis for Interviews
- LeetCode API Integration
- HackerRank API Integration
- AI Career Suggestions
- Email Notifications
- Cloud Sync
- Mobile App Version

---

## 🔀 Git/GitHub Workflow

- **Before starting work each day:** `Fetch origin` → `Pull origin`
- **After finishing a change:** Write a clear summary → `Commit to main` → `Push origin`
- **Avoid editing the same file as your teammate** — stick to your own modules above
- Use GitHub Desktop (no need for command-line Git)

---

## ✅ Foundation Already Built

- 5-layer MVC architecture with design patterns
- Maven project skeleton
- 14-table normalized SQLite schema
- Full light/dark theming system with custom Swing components: `RoundedCard`, `ModernButton`, `SidebarButton`, `CircularProgressBar`, `ThemeToggleSwitch`

All new modules should be built on top of this foundation.
