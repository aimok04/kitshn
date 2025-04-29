module Fastlane
  module Actions
    class CheckAndCommitReleaseAction < Action
      def self.run(params)
        androidChangelogsDir = "fastlane/metadata/android/en-US/changelogs/"
      
        changelogs = Dir.entries("fastlane/metadata/android/en-US/changelogs/")
        for changelog in changelogs do
          if changelog.length < 9 then
            next
          end
          
          filename = androidChangelogsDir + changelog
        
          size = File.size(filename)
          if size >= 500 then
            UI.user_error!("Changelog \"" + filename + "\" exceeds 500 byte limit (" + size.to_s() + " bytes).") 
            return
          end
        end
      
        system("git", "add", "composeApp/build.gradle.kts")
        system("git", "add", "iosApp/iosApp.xcodeproj/project.pbxproj")
        system("git", "add", "fastlane/metadata/changelog.md")
        system("git", "add", "fastlane/metadata/en-US/release_notes.txt")
        system("git", "add", androidChangelogsDir + changelogs[-1])
        
        version = other_action.retrieve_version()
        system("git", "commit", "-m", "release: " + version)
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Commit release'
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
