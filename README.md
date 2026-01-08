# 🚀 EduVerse - AI-Powered Learning App

![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Gemini AI](https://img.shields.io/badge/Google-Gemini%20AI-4285F4?style=for-the-badge&logo=google&logoColor=white)
![YouTube API](https://img.shields.io/badge/YouTube-API-FF0000?style=for-the-badge&logo=youtube&logoColor=white)

## 📱 Overview
EduVerse is a cutting-edge Android learning app that **instantly generates complete courses** using AI. Think of having a personal tutor in your pocket! 🎓

---

## 🎯 **Key Features at a Glance**

### 🎨 **Modern UI/UX**
- 🌙 **Dark/Light Themes** - Eye-friendly themes for any time
- 📱 **Responsive Design** - Perfect on phones and tablets
- ✨ **Smooth Animations** - Fluid transitions and feedback
- 🎯 **Tab Navigation** - Easy switching between course sections

### 🎨 **📸 Preview**
<img width="1200" height="630" alt="image" src="https://github.com/user-attachments/assets/ecfc1df0-7b83-4a3b-afa1-580e50fd6459" />

---

### 📚 **Smart Course Management**
| Feature | Description | Emoji |
|---------|-------------|-------|
| **Instant Courses** | Generate any course in seconds | 🤖⚡ |
| **Progress Tracking** | Visual progress bars & stats | 📊✅ |
| **Module System** | Organized learning paths | 📑🔗 |
| **Interactive Quizzes** | AI-generated MCQ tests | ❓✅ |
| **Video Library** | Curated YouTube content | 🎬📹 |

### 🔐 **Smart Features**
- 💾 **Local Storage** - Courses saved offline with Room Database
- 🔄 **Sync Ready** - Works offline, syncs when online
- 📁 **File Management** - Local file storage on device
- ⚡ **Fast Loading** - Instant access to saved content

---

## 🏗️ **Tech Stack**

### 🤖 **AI & APIs**
```yaml
AI Engine:      Google Gemini API
Video Content:  YouTube Data API
Local Storage:  Room Database
JSON Parsing:   Gson Library
```

### 📱 **Android Architecture**
```
📦 com.example.proj
 ┣ 📂 activities              # UI Screens
 ┃ ┣ 📜 CourseDetailActivity  # 📖 Course Viewer (Main Screen!)
 ┃ ┣ 📜 YouTubePlayerActivity # 🎬 In-App Video Player
 ┃ ┗ 📜 ...Other Activities
 ┣ 📂 adapters                # 📊 RecyclerView Adapters
 ┃ ┣ 📜 ModulesAdapter       # 🎯 Course Modules
 ┃ ┣ 📜 MCQsAdapter          # ❓ Quiz Questions  
 ┃ ┗ 📜 VideosAdapter        # 🎥 Video Suggestions
 ┣ 📂 models                  # 📝 Data Structures
 ┃ ┣ 📜 Course              # 🏫 Course Info
 ┃ ┣ 📜 CourseModule        # 📑 Learning Modules
 ┃ ┣ 📜 MCQQuestion         # ❓ Quiz Items
 ┃ ┗ 📜 VideoSuggestion     # 🎬 YouTube Videos
 ┣ 📂 database               # 💾 Local Storage
 ┃ ┣ 📜 CourseContentEntity # 🧠 AI-Generated Content
 ┃ ┣ 📜 CourseDao           # 📊 Database Operations
 ┃ ┗ 📜 AppDatabase         # 🗄️ Database Instance
 ┣ 📂 viewmodel              # ⚙️ Business Logic
 ┃ ┣ 📜 CourseViewModel     # 🎯 Course Operations
 ┃ ┗ 📜 ViewModelFactory    # 🏭 Factory Pattern
 ┗ 📂 repository             # 🔄 Data Management
   ┗ 📜 CourseRepository    # 💼 Repository Pattern
```

---

## 🎬 **User Experience**

### 📖 **Course Viewing Flow**
```
1️⃣ Open Course → 2️⃣ AI Content Loads → 3️⃣ Choose Tab → 4️⃣ Learn!
   │                │                    │
   📱 App         🤖 AI Processing     🎯 Your Choice
```

### 🎯 **Four Learning Tabs**
| Tab | Content | Emoji |
|-----|---------|-------|
| **Overview** | Course introduction & details | 📖✨ |
| **Modules** | Structured learning units | 📑🎯 |
| **Assessments** | Interactive quizzes | ❓✅ |
| **Videos** | Curated YouTube content | 🎬📹 |

### 🎥 **Video Playback Options**
```
Click Video → App Checks → Plays in App → OR → Opens YouTube
              │              🎬 In-App          🚀 External
              └──> Has Video ID? ──┬──> Yes ──> In-App Player
                                   └──> No ──> YouTube Search
```

---

## ⚡ **Performance & Optimization**

### 🚀 **Fast & Efficient**
- ⚡ **Instant Loading**: Local database for immediate access
- 💾 **Smart Caching**: AI content saved locally
- 📱 **Memory Efficient**: Proper lifecycle management
- 🔄 **Background Processing**: Non-blocking UI

### 🔧 **Technical Highlights**
```kotlin
// Clean Architecture
UI ← ViewModel ← Repository ← Database/API

// Offline First
Room Database → Local Storage → Fast Access

// Modern Android
Coroutines + Flow + LiveData + Material Design 3
```

---

## 🔧 **Getting Started**

### 📋 **Prerequisites**
```bash
Android Studio Flamingo+
Android SDK 33+
Gemini API Key
```

### 🚀 **Quick Setup**
1. **Clone the repo**
   ```bash
   git clone https://github.com/yourusername/eduverse.git
   ```
2. **Add API Key**
   ```properties
   # local.properties
   GEMINI_API_KEY=your_key_here
   ```
3. **Run the app** 🎉

### 🛠️ **Build Configuration**
```gradle
dependencies {
    // Room Database
    implementation "androidx.room:room-ktx:2.5.2"
    
    // YouTube Player
    implementation 'com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0'
    
    // Material Design
    implementation 'com.google.android.material:material:1.9.0'
}
```

---

## 📊 **Project Stats**

```
🎯 Features:       20+
📱 Screens:        6+
🧩 Components:     50+
📦 Dependencies:   15+
🏗️ Architecture:   MVVM + Repository
💾 Database:       Room
🎨 UI Framework:   XML + Material 3
```

---

## 🌟 **Why Choose EduVerse?**

| Feature | EduVerse | Others |
|---------|----------|--------|
| **AI Course Generation** | ✅ Instant | ❌ Manual |
| **Video Integration** | ✅ In-app | ❌ External |
| **Offline Support** | ✅ Full | ⚠️ Limited |
| **Modern UI** | ✅ Material 3 | ⚠️ Outdated |
| **Progress Tracking** | ✅ Visual | ❌ Basic |

---

## 🎯 **Use Cases**

### 👨‍🎓 **For Students**
- 📚 **Learn Anything**: Generate courses on any topic
- 🎯 **Structured Learning**: Follow organized modules
- 📊 **Track Progress**: Visual completion metrics
- 🎬 **Learn Visually**: Video content for better understanding

### 👨‍🏫 **For Educators**
- ⚡ **Quick Content Creation**: Generate course material instantly
- 🎯 **Structured Curriculum**: AI organizes content logically
- 📹 **Multimedia Integration**: Videos + Text + Quizzes
- 📱 **Mobile Ready**: Access courses anywhere

---

## 🚀 **Future Roadmap**

### 🎯 **Planned Features**
```yaml
Phase 1: ✅ Current Features
  - AI Course Generation
  - Video Integration
  - Progress Tracking
  
Phase 2: 🚧 In Development  
  - Voice Notes Recording
  - Study Groups
  - Gamification System
  
Phase 3: 📅 Planned
  - AR/VR Learning
  - Multi-language Support
  - Advanced Analytics
```

### 🔥 **Coming Soon**
- 🎮 **Learning Games** - Gamified quizzes
- 🤝 **Study Groups** - Collaborative learning
- 🎤 **Voice Notes** - Record audio summaries
- 📊 **Advanced Stats** - Learning analytics

---

## 🤝 **Contributing**

We love contributors! Here's how to help:

```bash
# 1. Fork the project
# 2. Create your feature branch
git checkout -b feature/AmazingFeature

# 3. Commit your changes
git commit -m 'Add AmazingFeature'

# 4. Push to the branch
git push origin feature/AmazingFeature

# 5. Open a Pull Request
```

---

## 📞 **Support & Contact**

- **GitHub Issues**: [Report Bug](https://github.com/Farid-xD)
- **Email**: Fariduddinmuhammad9@gmail.com
- **company**: [UraanStudios](https://github.com/uraan-studios)

---

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## ✨ **Acknowledgments**

- **Google** for Gemini AI API
- **YouTube** for Video API
- **Android Team** for Jetpack Libraries
- **Material Design** for beautiful components

---

<div align="center">

## ⭐ **Give us a star!** ⭐

If you find this project useful, please give it a star on GitHub!

[⭐ Star on GitHub](https://github.com/yourusername/eduverse)

---

### Made with ❤️ for the global learning community

**Learn Anything, Anytime, Anywhere** 📱🎓🚀

</div>
