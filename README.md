🎮 GPU Consultant
https://img.shields.io/badge/Java-21%252B-blue
https://img.shields.io/badge/Spring%2520Boot-3.5.5-brightgreen
https://img.shields.io/badge/SQLite-3.40-lightgrey
https://img.shields.io/badge/License-MIT-yellow
https://img.shields.io/badge/Interface-CLI-black

Professional GPU comparison and consultation system with intelligent search, advanced list management, and multi-format export capabilities.

✨ Key Features
🔍 Intelligent Search System
Context-aware searching with automatic suggestions

Manufacturer detection (AMD Radeon, NVIDIA GeForce, Intel Arc)

Relevance filtering - Prevents overly broad searches

Priority results with exact match prioritization

📊 Advanced List Management
Persistent storage - Lists saved between sessions

Multiple simultaneous lists with custom naming

Optimized 10-GPU limit per list for effective comparisons

Full CRUD operations - Add, remove, rename, switch

📁 Multi-Format Export
JSON format - Readable and portable structure

Excel format - Professional tables for analysis

Smart export routing to optimal directories (Downloads/Desktop)

Customizable naming for exported files

⚡ PowerShell-Style CLI
Intuitive command structure - Consistent and predictable

Integrated help system - Contextual suggestions

Index-based operations - No more long name typing

Real-time status updates - Immediate feedback

🚀 Quick Start
Prerequisites
Java 21 or higher

Maven 3.6+ (for compilation)

Installation & Execution
bash
# Clone the repository
git clone https://github.com/yourusername/gpu-consultant.git
cd gpu-consultant

# Compile the application
mvn clean package -DskipTests

# Run the JAR
java -jar target/gpu-consultant-1.0.0.jar
Direct Execution (Precompiled JAR)
bash
java -jar gpu-consultant-1.0.0.jar
📚 Core Commands
Search & Consultation
bash
search "rtx 4060"          # Search GPUs
search "radeon rx 6700" -a # Search and auto-add to list
gpu show 1                 # View details by index
gpu compare "RTX 4060" "RX 7700 XT"  # Compare two GPUs
results                    # View last search results
List Management
bash
list new "Gaming 2024"     # Create new list
list add "RTX 4060"        # Add GPU to current list  
list status                # Show list status
list show                  # Show detailed list content
list switch "Workstation"  # Switch between lists
list all                   # Show all saved lists
Export & Utilities
bash
list export -f xlsx        # Export to Excel
list export -f json        # Export to JSON
list export -o "comparison" -d "~/Documents" # Custom export
suggest                   # Contextual suggestions
help                      # Complete help guide
exit                      # Exit application
🗃️ Project Structure
text
gpu-consultant/
├── src/main/java/GpuIndex/App/
│   ├── controller/        # CLI and REST controllers
│   ├── service/          # Business logic
│   ├── model/            # Entities and DTOs
│   ├── repository/       # Data access
│   └── config/           # Configuration classes
├── src/main/resources/
│   ├── application.yml   # Main configuration
│   └── gpu_database.json # GPU database
├── saved_lists/          # Persisted lists (auto-generated)
└── target/               # Build output
🎯 Usage Examples
Typical Workflow:
bash
# 1. Search for GPUs of interest
search "rtx 4060"
search "rx 7700 xt" -a

# 2. View details and compare
gpu show 1
gpu compare "RTX 4060" "RX 7700 XT"

# 3. Create comparison list
list new "Mid-Range Comparison"
list add "RTX 4060"
list add "RX 7700 XT"

# 4. Export results
list export -f xlsx -o "mid_range_comparison"
🔧 Technology Stack
Spring Boot 3.5.5 - Main framework

Spring Shell - Command-line interface

SQLite - Embedded database

Hibernate - ORM and entity management

Apache POI - Excel export functionality

Jackson - JSON serialization

Caffeine - Caching system

📊 Database Coverage
Includes comprehensive JSON database with:

✅ AMD Radeon (RX 5000, 6000, 7000 series)

✅ NVIDIA GeForce (GTX 16, RTX 20, 30, 40 series)

✅ Intel Arc (A-series)

✅ Complete specifications: Performance, memory, TDP, clocks

⚡ Performance Metrics
Search operations: < 100ms response time

Application startup: 2-3 seconds

Excel export: 1-2 seconds

Persistence: Automatic and transparent

🤝 Contributing
Contributions are welcome! For major changes:

Fork the project

Create a feature branch (git checkout -b feature/AmazingFeature)

Commit your changes (git commit -m 'Add AmazingFeature')

Push to the branch (git push origin feature/AmazingFeature)

Open a Pull Request

📝 License
This project is licensed under the MIT License - see the LICENSE file for details.

🆘 Support
If you encounter issues:

Check integrated help (help command)

Use suggest for contextual guidance

Verify Java 21+ installation

Check existing GitHub issues

GPU Consultant - Your intelligent assistant for the GPU world 🚀

📞 Contact
For questions and support:

GitHub Issues: Project Issues

Email: your-email@example.com

🙏 Acknowledgments
Spring Boot team for the excellent framework

SQLite for lightweight database solution

Hardware manufacturers for specification data

⭐ If you find this project useful, please give it a star on GitHub!
