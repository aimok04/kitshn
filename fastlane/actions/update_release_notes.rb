module Fastlane
  module Actions
    class UpdateReleaseNotesAction < Action
      def self.run(params)
        File.write(
          "fastlane/metadata/en-US/release_notes.txt",
          File.read("fastlane/metadata/en-US/release_notes.txt") + "\n\n-- adapt for " + params[:version_code].to_s()
        )
      
        androidChangelogsDir = "fastlane/metadata/android/en-US/changelogs/"
      
        latestAndroidChangelogFile = androidChangelogsDir + Dir.entries(androidChangelogsDir)[-1]
        newAndroidChangelogFile = androidChangelogsDir + params[:version_code].to_s() + ".txt"
        
        File.write(
          newAndroidChangelogFile, 
          File.read(latestAndroidChangelogFile) + "\n\n-- adapt for " + params[:version_code].to_s()
        )
        
        system("git", "add", newAndroidChangelogFile)
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.available_options
        [
          "version_code"
        ]
      end

      def self.description
        'Update changelog.md'
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
